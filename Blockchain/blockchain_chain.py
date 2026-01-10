import argparse
import hashlib
import json
import os
import sys
import threading
import time
from dataclasses import asdict, dataclass
from datetime import datetime, timedelta, timezone
from typing import List, Optional

from mpi4py import MPI

sys.stdout.reconfigure(encoding="utf-8")



def utcnow() -> datetime:
    return datetime.now(timezone.utc)


def dt_to_iso(dt: datetime) -> str:
    """UTC ISO format"""
    return dt.astimezone(timezone.utc).isoformat()


def iso_to_dt(s: str) -> datetime:
    return datetime.fromisoformat(s).astimezone(timezone.utc)


def sha256_hex(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest().upper()


# ----------------------------
# Block
# ----------------------------

@dataclass
class Block:
    index: int
    data: str
    timestamp: str          # ISO string (UTC)
    previous_hash: str
    difficulty: int
    nonce: int
    hash: str

    @staticmethod
    def compute_hash(
        index: int,
        data: str,
        timestamp_iso: str,
        previous_hash: str,
        difficulty: int,
        nonce: int,
    ) -> str:
        inp = f"{index}{data}{timestamp_iso}{previous_hash}{difficulty}{nonce}"
        return sha256_hex(inp)

    def recompute_hash(self) -> str:
        return Block.compute_hash(
            self.index,
            self.data,
            self.timestamp,
            self.previous_hash,
            self.difficulty,
            self.nonce,
        )


# ----------------------------
# Blockchain
# ----------------------------

class Blockchain:
    def __init__(
        self,
        difficulty: int = 5,
        block_generation_interval: int = 10,
        difficulty_adjustment_interval: int = 10,
    ):
        self.chain: List[Block] = []
        self.difficulty = difficulty
        self.block_generation_interval = block_generation_interval
        self.difficulty_adjustment_interval = difficulty_adjustment_interval
        self.chain.append(self.create_genesis_block())

    def create_genesis_block(self) -> Block:
        ts = datetime(1970, 1, 1, tzinfo=timezone.utc)
        timestamp_iso = dt_to_iso(ts)

        nonce = 0
        h = Block.compute_hash(
            0, "Genesis Block", timestamp_iso, "0", self.difficulty, nonce
        )

        target = "0" * self.difficulty
        while not h.startswith(target):
            nonce += 1
            h = Block.compute_hash(
                0, "Genesis Block", timestamp_iso, "0", self.difficulty, nonce
            )

        return Block(
            index=0,
            data="Genesis Block",
            timestamp=timestamp_iso,
            previous_hash="0",
            difficulty=self.difficulty,
            nonce=nonce,
            hash=h,
        )

    def get_latest_block(self) -> Block:
        return self.chain[-1]

    def get_chain(self) -> List[Block]:
        return self.chain

    def calculate_cumulative_diff(self, chain: List[Block]) -> float:
        return sum((2 ** b.difficulty) for b in chain)

    def adjust_difficulty(self, proposed_diff: int) -> int:
        if len(self.chain) <= self.difficulty_adjustment_interval:
            return proposed_diff

        mined_blocks_num = len(self.chain) - 1
        if mined_blocks_num % self.difficulty_adjustment_interval != 0:
            return proposed_diff

        adjustment_block = self.chain[
            len(self.chain) - self.difficulty_adjustment_interval - 1
        ]
        latest_block = self.chain[-1]

        expected_time = timedelta(
            seconds=self.block_generation_interval
            * self.difficulty_adjustment_interval
        )
        time_taken = (
            iso_to_dt(latest_block.timestamp)
            - iso_to_dt(adjustment_block.timestamp)
        )

        base_difficulty = adjustment_block.difficulty

        if time_taken < expected_time / 2:
            return base_difficulty + 1
        elif time_taken > expected_time * 2:
            return max(1, base_difficulty - 1)
        else:
            return base_difficulty

    def validate_chain(self, chain_to_validate: List[Block]) -> bool:
        if not chain_to_validate:
            return False

        now = utcnow()

        genesis = chain_to_validate[0]
        if genesis.index != 0 or genesis.previous_hash != "0":
            return False

        if not genesis.hash.startswith("0" * genesis.difficulty):
            return False

        if genesis.hash != genesis.recompute_hash():
            return False

        try:
            g_ts = iso_to_dt(genesis.timestamp)
        except Exception:
            return False

        if g_ts > now + timedelta(minutes=1):
            return False

        for i in range(1, len(chain_to_validate)):
            current = chain_to_validate[i]
            previous = chain_to_validate[i - 1]

            if current.index != previous.index + 1:
                return False
            if current.previous_hash != previous.hash:
                return False

            cur_ts = iso_to_dt(current.timestamp)
            prev_ts = iso_to_dt(previous.timestamp)

            if cur_ts > now + timedelta(minutes=1):
                return False
            if cur_ts < prev_ts - timedelta(minutes=1):
                return False

            if not current.hash.startswith("0" * current.difficulty):
                return False
            if current.hash != current.recompute_hash():
                return False

        return True

    def add_block(self, new_block: Block) -> bool:
        temp = list(self.chain)
        temp.append(new_block)
        if self.validate_chain(temp):
            self.chain.append(new_block)
            return True
        return False

    def replace_chain(self, new_chain: List[Block]) -> bool:
        if not new_chain:
            return False

        if (
            self.calculate_cumulative_diff(new_chain)
            > self.calculate_cumulative_diff(self.chain)
            and self.validate_chain(new_chain)
        ):
            self.chain = new_chain
            return True

        return False

    def to_json(self) -> str:
        return json.dumps([asdict(b) for b in self.chain], separators=(",", ":"))

    @staticmethod
    def from_json(s: str) -> List[Block]:
        raw = json.loads(s)
        return [Block(**item) for item in raw]


# ----------------------------
# Parallel mining
# ----------------------------

def mine_block_parallel(
    latest: Block,
    data: str,
    difficulty: int,
    rank: int,
    world_size: int,
    thread_count: int,
    stop_event: threading.Event,
) -> Optional[Block]:

    target = "0" * difficulty
    index = latest.index + 1
    previous_hash = latest.hash
    timestamp_iso = dt_to_iso(utcnow())

    found_lock = threading.Lock()
    found = {"block": None}

    step = world_size * thread_count

    def worker(tid: int):
        start_nonce = rank * thread_count + tid
        nonce = start_nonce

        while not stop_event.is_set():
            h = Block.compute_hash(
                index,
                data,
                timestamp_iso,
                previous_hash,
                difficulty,
                nonce,
            )
            if h.startswith(target):
                with found_lock:
                    if found["block"] is None:
                        found["block"] = Block(
                            index=index,
                            data=data,
                            timestamp=timestamp_iso,
                            previous_hash=previous_hash,
                            difficulty=difficulty,
                            nonce=int(nonce),
                            hash=h,
                        )
                        stop_event.set()
                return
            nonce += step

    threads = []
    for tid in range(thread_count):
        t = threading.Thread(target=worker, args=(tid,), daemon=True)
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    return found["block"]


# ----------------------------
# MPI communication
# ----------------------------

TAG_CHAIN = 100


def broadcast_chain_to_all(comm: MPI.Comm, chain_json: str, rank: int, world_size: int):
    for r in range(world_size):
        if r != rank:
            comm.send(chain_json, dest=r, tag=TAG_CHAIN)


def poll_incoming_chains(
    comm: MPI.Comm,
    blockchain: Blockchain,
    chain_lock: threading.Lock,
    mining_stop: threading.Event,
) -> bool:

    replaced_any = False
    status = MPI.Status()

    while comm.iprobe(source=MPI.ANY_SOURCE, tag=TAG_CHAIN, status=status):
        src = status.Get_source()
        msg = comm.recv(source=src, tag=TAG_CHAIN)

        try:
            incoming_chain = Blockchain.from_json(msg)
        except Exception:
            continue

        with chain_lock:
            if blockchain.replace_chain(incoming_chain):
                replaced_any = True
                mining_stop.set()

    return replaced_any


def print_block(b: Block, rank: int):
    print(
        "\n--- VALID BLOK NAJDEN/DODAN ---\n"
        f"rank: {rank}\n"
        f"index: {b.index}\n"
        f"timestamp(UTC): {b.timestamp}\n"
        f"difficulty: {b.difficulty}\n"
        f"nonce: {b.nonce}\n"
        f"previous_hash: {b.previous_hash}\n"
        f"hash: {b.hash}\n"
        f"data: {b.data}\n"
        "------------------------------",
        flush=True,
    )


# ----------------------------
# Main
# ----------------------------

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--blocks", type=int, default=20)
    parser.add_argument("--threads", type=int, default=0)
    parser.add_argument("--difficulty", type=int, default=5)
    parser.add_argument("--gen-interval", type=int, default=10)
    parser.add_argument("--adj-interval", type=int, default=10)
    args = parser.parse_args()

    comm = MPI.COMM_WORLD
    rank = comm.Get_rank()
    world_size = comm.Get_size()

    thread_count = args.threads if args.threads > 0 else (os.cpu_count() or 1)

    blockchain = Blockchain(
        difficulty=args.difficulty,
        block_generation_interval=args.gen_interval,
        difficulty_adjustment_interval=args.adj_interval,
    )

    chain_lock = threading.Lock()

    if rank == 0:
        t0 = time.perf_counter()

    while True:
        with chain_lock:
            latest = blockchain.get_latest_block()
            diff = blockchain.adjust_difficulty(latest.difficulty)
            if latest.index >= args.blocks:
                break

        mining_stop = threading.Event()

        def listener():
            while not mining_stop.is_set():
                poll_incoming_chains(
                    comm, blockchain, chain_lock, mining_stop
                )
                time.sleep(0.01)

        listener_thread = threading.Thread(target=listener, daemon=True)
        listener_thread.start()

        new_block = mine_block_parallel(
            latest,
            "Block data",
            diff,
            rank,
            world_size,
            thread_count,
            mining_stop,
        )

        mining_stop.set()
        listener_thread.join(timeout=0.1)

        if new_block is not None:
            with chain_lock:
                current_latest = blockchain.get_latest_block()
                if (
                    current_latest.hash == new_block.previous_hash
                    and current_latest.index + 1 == new_block.index
                ):
                    if blockchain.add_block(new_block):
                        print_block(new_block, rank)
                        broadcast_chain_to_all(
                            comm,
                            blockchain.to_json(),
                            rank,
                            world_size,
                        )

        time.sleep(0.001)

    comm.barrier()

    if rank == 0:
        t1 = time.perf_counter()
        with chain_lock:
            print("\n=== REZULTAT ===")
            print(f"nodes: {world_size}")
            print(f"threads per node: {thread_count}")
            print(f"final index: {blockchain.get_latest_block().index}")
            print(f"chain length: {len(blockchain.get_chain())}")
            print(
                f"cumulative diff: "
                f"{blockchain.calculate_cumulative_diff(blockchain.get_chain()):.2f}"
            )
            print(f"time: {t1 - t0:.3f} s")


if __name__ == "__main__":
    main()
