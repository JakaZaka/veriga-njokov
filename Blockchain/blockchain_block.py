import argparse
from dataclasses import asdict, dataclass
from datetime import datetime, timedelta, timezone
import hashlib
import json
import os
import threading
import time
from typing import List, Optional
from mpi4py import MPI

import sys
sys.stdout.reconfigure(encoding="utf-8")

# ----------------------------
# Utils
# ----------------------------

def utcnow() -> datetime:
    return datetime.now(timezone.utc) # ni problem z timezone-i

def dt_to_iso(dt: datetime) -> str:
    # UTC ISO format
    return dt.astimezone(timezone.utc).isoformat()

def iso_to_dt(s: str) -> datetime:
    return datetime.fromisoformat(s).astimezone(timezone.utc)

def sha256_hex(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest().upper()


# ----------------------------
# BLOCK
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
    def compute_hash(index: int, data: str, timestamp_iso: str, previous_hash: str, difficulty: int, nonce: int) -> str:
        inp = f"{index}{data}{timestamp_iso}{previous_hash}{difficulty}{nonce}"
        return sha256_hex(inp)

    def recompute_hash(self) -> str:
        return Block.compute_hash(self.index, self.data, self.timestamp, self.previous_hash, self.difficulty, self.nonce)


# ----------------------------
# Blockchain
# ----------------------------

class Blockchain:
    def __init__(self, difficulty: int = 5, block_generation_interval: int = 10, difficulty_adjustment_interval: int = 10): #konstruktor
        self.chain: List[Block] = []
        self.difficulty = difficulty
        self.block_generation_interval = block_generation_interval
        self.difficulty_adjustment_interval = difficulty_adjustment_interval

        self.chain.append(self.create_genesis_block())
        self.cumulative_diff: float = float(sum((2 ** b.difficulty) for b in self.chain))

    def create_genesis_block(self) -> Block:
        # genesis timestamp = UnixEpoch, prev_hash = "0"
        ts = datetime(1970, 1, 1, tzinfo=timezone.utc)
        timestamp_iso = dt_to_iso(ts)

        nonce = 0
        h = Block.compute_hash(0, "Genesis Block", timestamp_iso, "0", self.difficulty, nonce)

        #pow mining
        target = "0" * self.difficulty
        while not h.startswith(target):
            nonce += 1
            h = Block.compute_hash(0, "Genesis Block", timestamp_iso, "0", self.difficulty, nonce)

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
        if chain is self.chain:
            return float(self.cumulative_diff)
        return float(sum((2 ** b.difficulty) for b in chain))

    def adjust_difficulty(self, proposed_diff: int) -> int:
        """
        prilagoditvani blok = Veriga blokov[dolžina verige - interval popravka]
        pričakovani čas = čas ustvarjanja bloka * interval popravka
        dejanski čas = časovna značka zadnjega bloka - časovna značka prilagoditbenega bloka

        if ( dejanski čas < (pričakovani čas / 2) ) return težavnost prilagoditvenega bloka + 1 
        else if ( dejanski čas > (pričakovani čas * 2) ) return težavnost prilagoditvenega bloka - 1
        else return težavnost prilagoditvenega bloka
        """
        if len(self.chain) <= self.difficulty_adjustment_interval:
            return proposed_diff

        mined_blocks_num = len(self.chain) - 1
        if mined_blocks_num % self.difficulty_adjustment_interval != 0:
            return proposed_diff

        adjustment_block = self.chain[len(self.chain) - self.difficulty_adjustment_interval - 1]
        latest_block = self.chain[-1]

        expected_time = timedelta(seconds=self.block_generation_interval * self.difficulty_adjustment_interval)
        time_taken = iso_to_dt(latest_block.timestamp) - iso_to_dt(adjustment_block.timestamp)

        base_difficulty = adjustment_block.difficulty

        if time_taken < expected_time / 2:
            return base_difficulty + 1
        elif time_taken > expected_time * 2:
            return max(1, base_difficulty - 1)
        else:
            return base_difficulty

    # ---- validacija cele verige (za replace_chain fallback) ----

    def validate_chain(self, chain_to_validate: List[Block]) -> bool:
        if not chain_to_validate:
            return False

        now = utcnow()

        #validacija genesis
        genesis = chain_to_validate[0]
        if genesis.index != 0:
            return False
        if genesis.previous_hash != "0":
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

        #validacija ostalih blokov
        for i in range(1, len(chain_to_validate)):
            current = chain_to_validate[i]
            previous = chain_to_validate[i - 1]

            if current.index != previous.index + 1:
                return False
            if current.previous_hash != previous.hash:
                return False

            try:
                cur_ts = iso_to_dt(current.timestamp)
                prev_ts = iso_to_dt(previous.timestamp)
            except Exception:
                return False

            if cur_ts > now + timedelta(minutes=1):
                return False
            if cur_ts < prev_ts - timedelta(minutes=1):
                return False

            if not current.hash.startswith("0" * current.difficulty):
                return False
            if current.hash != current.recompute_hash():
                return False

        return True


    def validate_next_block(self, new_block: Block, now: Optional[datetime] = None) -> bool:
        if not self.chain:
            return False

        now = now or utcnow()
        prev = self.get_latest_block()

        if new_block.index != prev.index + 1:
            return False
        if new_block.previous_hash != prev.hash:
            return False

        try:
            cur_ts = iso_to_dt(new_block.timestamp)
            prev_ts = iso_to_dt(prev.timestamp)
        except Exception:
            return False

        if cur_ts > now + timedelta(minutes=1): # 1 min tolerance
            return False
        if cur_ts < prev_ts - timedelta(minutes=1):
            return False

        if not new_block.hash.startswith("0" * new_block.difficulty):
            return False
        if new_block.hash != new_block.recompute_hash():
            return False

        return True

    def add_block_fast(self, new_block: Block) -> bool:
        if self.validate_next_block(new_block):
            self.chain.append(new_block)
            self.cumulative_diff += (2 ** new_block.difficulty)
            return True
        return False

    def replace_chain(self, new_chain: List[Block]) -> bool:
        if not new_chain:
            return False

        if (self.calculate_cumulative_diff(new_chain) > self.calculate_cumulative_diff(self.chain)
                and self.validate_chain(new_chain)):
            self.chain = new_chain
            self.cumulative_diff = float(sum((2 ** b.difficulty) for b in self.chain))
            return True
        return False

    def to_json(self) -> str: #pretvori verigo v json... za posiljanje prek MPI
        return json.dumps([asdict(b) for b in self.chain], separators=(",", ":"))

    @staticmethod
    def from_json(s: str) -> List[Block]:
        raw = json.loads(s)
        return [Block(**item) for item in raw]


# ----------------------------
# Mining: MPI (processes) + threads
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
    """
    Returns new block if found, otherwise None (if stop_event is set).
    Nonce space: start = rank*T + tid, step = P*T
    """
    target = "0" * difficulty
    index = latest.index + 1
    previous_hash = latest.hash

    # freeze timestamp for this mining attempt
    timestamp_iso = dt_to_iso(utcnow())

    found_lock = threading.Lock()
    found: dict = {"block": None}

    step = world_size * thread_count

    def worker(tid: int):
        start_nonce = rank * thread_count + tid
        nonce = start_nonce
        while not stop_event.is_set():
            h = Block.compute_hash(index, data, timestamp_iso, previous_hash, difficulty, nonce)
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
# MPI messaging: send block normally, request full chain as fallback
# ----------------------------

TAG_BLOCK = 101
TAG_CHAIN_REQ = 102
TAG_CHAIN_RESP = 103

def block_to_json(b: Block) -> str:
    return json.dumps(asdict(b), separators=(",", ":"))

def block_from_json(s: str) -> Block:
    return Block(**json.loads(s))

def broadcast_block_to_all(comm: MPI.Comm, block_json: str, rank: int, world_size: int):
    for r in range(world_size):
        if r != rank:
            comm.send(block_json, dest=r, tag=TAG_BLOCK)

def request_chain_from(comm: MPI.Comm, dest_rank: int):
    comm.send("give_chain", dest=dest_rank, tag=TAG_CHAIN_REQ)

def send_chain_to(comm: MPI.Comm, dest_rank: int, blockchain: Blockchain):
    comm.send(blockchain.to_json(), dest=dest_rank, tag=TAG_CHAIN_RESP)

def poll_incoming_messages(
    comm: MPI.Comm,
    blockchain: Blockchain,
    chain_lock: threading.Lock,
    mining_stop: threading.Event,
) -> bool:
    """
    Handles:
      - TAG_BLOCK: receive a single new block (fast path)
      - TAG_CHAIN_REQ: peer requests full chain (fallback)
      - TAG_CHAIN_RESP: receive full chain (fallback)
    If chain changes (block added or chain replaced), sets mining_stop and returns True.
    """
    changed = False
    status = MPI.Status()

    while comm.iprobe(source=MPI.ANY_SOURCE, tag=MPI.ANY_TAG, status=status):
        src = status.Get_source()
        tag = status.Get_tag()

        if tag == TAG_BLOCK:
            msg = comm.recv(source=src, tag=TAG_BLOCK)
            try:
                incoming_block = block_from_json(msg)
            except Exception:
                continue

            with chain_lock:
                latest = blockchain.get_latest_block()
                #ce sem zadaj, zahtevaj verigo
                if incoming_block.index > latest.index + 1:
                    request_chain_from(comm, src)
                    continue

                if blockchain.add_block_fast(incoming_block):
                    changed = True
                    mining_stop.set()
                else:
                    request_chain_from(comm, src)

        elif tag == TAG_CHAIN_REQ:
            _ = comm.recv(source=src, tag=TAG_CHAIN_REQ)
            with chain_lock:
                send_chain_to(comm, src, blockchain)

        elif tag == TAG_CHAIN_RESP:
            msg = comm.recv(source=src, tag=TAG_CHAIN_RESP)
            try:
                incoming_chain = Blockchain.from_json(msg)
            except Exception:
                continue

            with chain_lock:
                if blockchain.replace_chain(incoming_chain):
                    changed = True
                    mining_stop.set()

        else:
            # consume unknown message
            _ = comm.recv(source=src, tag=tag)

    return changed


def print_block(b: Block, rank: int):
    print(
        "\n--- VALID BLOCK ---\n"
        f"rank:           {rank}\n"
        f"index:          {b.index}\n"
        f"timestamp(UTC): {b.timestamp}\n"
        f"difficulty:     {b.difficulty}\n"
        f"nonce:          {b.nonce}\n"
        f"previous_hash:  {b.previous_hash}\n"
        f"hash:           {b.hash}\n"
        f"data:           {b.data}\n"
        "------------------------------",
        flush=True,
    )

# ----------------------------
# Main
# ----------------------------

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--blocks", type=int, default=20, help="How many blocks to mine")
    parser.add_argument("--threads", type=int, default=0, help="Number of threads per node (0 => os.cpu_count())")
    parser.add_argument("--difficulty", type=int, default=5, help="Initial difficulty (genesis)")
    parser.add_argument("--gen-interval", type=int, default=10, help="Target block time (seconds)")
    parser.add_argument("--adj-interval", type=int, default=10, help="Difficulty adjustment interval (in blocks)")
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

    mined_global_target = args.blocks

    while True:
        with chain_lock:
            latest = blockchain.get_latest_block()
            diff = blockchain.adjust_difficulty(latest.difficulty)

        if latest.index >= mined_global_target:
            break

        mining_stop = threading.Event()

        def listener():
            while not mining_stop.is_set():
                poll_incoming_messages(comm, blockchain, chain_lock, mining_stop)
                time.sleep(0.01)

        listener_thread = threading.Thread(target=listener, daemon=True)
        listener_thread.start()

        #mining večnitno
        new_block = mine_block_parallel(
            latest=latest,
            data="Block data",
            difficulty=diff,
            rank=rank,
            world_size=world_size,
            thread_count=thread_count,
            stop_event=mining_stop,
        )

        mining_stop.set()
        listener_thread.join(timeout=0.1)

        if new_block is not None:
            added = False
            with chain_lock:
                #preverim da se ni med miningom veriga spremenila
                current_latest = blockchain.get_latest_block()
                if current_latest.hash == new_block.previous_hash and current_latest.index + 1 == new_block.index:
                    added = blockchain.add_block_fast(new_block)

                bjson = block_to_json(new_block)

            if added: #lahko dodam and rank == 0 ce je motece da izpisuje iz vseh nodeov
                print_block(new_block, rank)
                broadcast_block_to_all(comm, bjson, rank, world_size)

        time.sleep(0.001)

    comm.barrier()

    if rank == 0:
        t1 = time.perf_counter()
        with chain_lock:
            final_len = len(blockchain.get_chain())
            final_idx = blockchain.get_latest_block().index
            cumdiff = blockchain.calculate_cumulative_diff(blockchain.get_chain())

        print("\n=== REZULTAT ===")
        print(f"nodes:            {world_size}")
        print(f"threads per node: {thread_count}")
        print(f"final index:      {final_idx}")
        print(f"chain length:     {final_len}")
        print(f"cumulative diff:  {cumdiff:.2f}")
        print(f"time:             {t1 - t0:.3f} s")


if __name__ == "__main__":
    main()
