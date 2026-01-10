import argparse
from dataclasses import asdict, dataclass
from datetime import datetime, timedelta, timezone
import hashlib
import json
import os
import threading
import time
from typing import Dict, List, Optional
from collections import deque

from mpi4py import MPI

# Web API (rank 0)
from flask import Flask, request, jsonify

import sys
sys.stdout.reconfigure(encoding="utf-8")


# ----------------------------
# Utils
# ----------------------------

def utcnow() -> datetime:
    return datetime.now(timezone.utc)

def dt_to_iso(dt: datetime) -> str:
    return dt.astimezone(timezone.utc).isoformat()

def iso_to_dt(s: str) -> datetime:
    # accept "...Z" as UTC too
    if isinstance(s, str) and s.endswith("Z"):
        s = s[:-1] + "+00:00"
    return datetime.fromisoformat(s).astimezone(timezone.utc)

def sha256_hex(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest().upper()

def canonical_json(obj) -> str:
    return json.dumps(obj, separators=(",", ":"), ensure_ascii=False, sort_keys=True)


# ----------------------------
# Blockchain data structures
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
        return Block.compute_hash(
            self.index, self.data, self.timestamp, self.previous_hash, self.difficulty, self.nonce
        )


class Blockchain:
    def __init__(self, difficulty: int = 5, block_generation_interval: int = 10, difficulty_adjustment_interval: int = 10):
        self.chain: List[Block] = []
        self.difficulty = difficulty
        self.block_generation_interval = block_generation_interval
        self.difficulty_adjustment_interval = difficulty_adjustment_interval
        self.chain.append(self.create_genesis_block())

    def create_genesis_block(self) -> Block:
        ts = datetime(1970, 1, 1, tzinfo=timezone.utc)
        timestamp_iso = dt_to_iso(ts)

        nonce = 0
        h = Block.compute_hash(0, "Genesis Block", timestamp_iso, "0", self.difficulty, nonce)

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
            hash=h
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

    def validate_chain(self, chain_to_validate: List[Block]) -> bool:
        if not chain_to_validate:
            return False

        now = utcnow()

        # --- VALIDACIJA GENESIS BLOKA ---
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

        # --- OSTALI BLOKI ---
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
        if not self.validate_chain(new_chain):
            return False

        new_cd = self.calculate_cumulative_diff(new_chain)
        cur_cd = self.calculate_cumulative_diff(self.chain)

        if new_cd > cur_cd:
            self.chain = new_chain
            return True

        # tie-break: če je enaka kumulativna težavnost, izberi verigo z "manjšim" zadnjim hashom
        if new_cd == cur_cd:
            if new_chain[-1].hash < self.chain[-1].hash:
                self.chain = new_chain
                return True

        return False

    def to_json(self) -> str:
        return json.dumps([asdict(b) for b in self.chain], separators=(",", ":"), ensure_ascii=False)

    @staticmethod
    def from_json(s: str) -> List[Block]:
        raw = json.loads(s)
        return [Block(**item) for item in raw]


# ----------------------------
# Mining (MPI + threads)
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
    Vrne nov blok, če ga najde; sicer None (če stop_event set).
    Nonce prostor: start = rank*T + tid, step = P*T
    """
    target = "0" * difficulty
    index = latest.index + 1
    previous_hash = latest.hash

    # timestamp "zamrznem" za ta mining attempt (različen po procesu, OK)
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
                            hash=h
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
# MPI Communication
# ----------------------------

TAG_CHAIN = 100
TAG_TARGET = 101
TAG_EVENT = 102
TAG_STOP = 103
TAG_BLOCK = 104  # kandidat blok (najden blok)


def broadcast_chain_to_all(comm: MPI.Comm, chain_json: str, rank: int, world_size: int):
    for r in range(world_size):
        if r != rank:
            comm.send(chain_json, dest=r, tag=TAG_CHAIN)

def broadcast_target_to_all(comm: MPI.Comm, target_index: int, rank: int, world_size: int):
    for r in range(world_size):
        if r != rank:
            comm.send(int(target_index), dest=r, tag=TAG_TARGET)

def broadcast_event_to_all(comm: MPI.Comm, index: int, data_str: str, rank: int, world_size: int):
    msg = {"index": int(index), "data": data_str}
    for r in range(world_size):
        if r != rank:
            comm.send(msg, dest=r, tag=TAG_EVENT)

def broadcast_stop_to_all(comm: MPI.Comm, rank: int, world_size: int):
    for r in range(world_size):
        if r != rank:
            comm.send(True, dest=r, tag=TAG_STOP)

def broadcast_block_to_all(comm: MPI.Comm, block: Block, rank: int, world_size: int):
    msg = asdict(block)
    for r in range(world_size):
        if r != rank:
            comm.send(msg, dest=r, tag=TAG_BLOCK)


def poll_incoming_messages(
    comm: MPI.Comm,
    blockchain: Blockchain,
    chain_lock: threading.Lock,
    mining_stop: threading.Event,
    shared_target: dict,
    events_by_index: Dict[int, str],
    events_lock: threading.Lock,
    global_stop: threading.Event,
    rank: int
) -> None:
    """
    Posluša:
      - TAG_BLOCK: kandidat blok -> poskusi add_block + stop mining
      - TAG_CHAIN: backup (ReplaceChain)
      - TAG_TARGET: nov target index
      - TAG_EVENT: podatki dogodka za konkreten block index
      - TAG_STOP: stop
    """
    status = MPI.Status()

    # 1) Kandidat blok (najhitrejša pot do konsenza)
    while comm.iprobe(source=MPI.ANY_SOURCE, tag=TAG_BLOCK, status=status):
        src = status.Get_source()
        msg = comm.recv(source=src, tag=TAG_BLOCK)
        try:
            b = Block(**msg)
        except Exception:
            continue

        accepted = False
        with chain_lock:
            latest = blockchain.get_latest_block()

            # ignore stale/duplicate
            if b.index <= latest.index:
                continue

            # accept only next block on our tip
            if b.index == latest.index + 1 and b.previous_hash == latest.hash:
                if blockchain.add_block(b):
                    accepted = True

        if accepted:
            mining_stop.set()
            if rank == 0:
                print_block(b, rank)
                # broadcast chain as backup for convergence
                with chain_lock:
                    chain_json = blockchain.to_json()
                broadcast_chain_to_all(comm, chain_json, rank, comm.Get_size())

    # 2) Chain (backup)
    while comm.iprobe(source=MPI.ANY_SOURCE, tag=TAG_CHAIN, status=status):
        src = status.Get_source()
        msg = comm.recv(source=src, tag=TAG_CHAIN)
        try:
            incoming_chain = Blockchain.from_json(msg)
        except Exception:
            continue

        with chain_lock:
            if blockchain.replace_chain(incoming_chain):
                mining_stop.set()

    # 3) Target
    while comm.iprobe(source=MPI.ANY_SOURCE, tag=TAG_TARGET, status=status):
        src = status.Get_source()
        t = comm.recv(source=src, tag=TAG_TARGET)
        try:
            t = int(t)
        except Exception:
            continue
        if t > shared_target["value"]:
            shared_target["value"] = t

    # 4) Event data
    while comm.iprobe(source=MPI.ANY_SOURCE, tag=TAG_EVENT, status=status):
        src = status.Get_source()
        msg = comm.recv(source=src, tag=TAG_EVENT)
        try:
            idx = int(msg["index"])
            data_str = str(msg["data"])
        except Exception:
            continue
        with events_lock:
            events_by_index.setdefault(idx, data_str)

    # 5) Stop
    while comm.iprobe(source=MPI.ANY_SOURCE, tag=TAG_STOP, status=status):
        _ = comm.recv(source=MPI.ANY_SOURCE, tag=TAG_STOP)
        global_stop.set()
        mining_stop.set()


# ----------------------------
# Printing
# ----------------------------

def print_block(b: Block, rank: int):
    print(
        "\n--- VALID BLOK NAJDEN/DODAN ---\n"
        f"rank:           {rank}\n"
        f"index:          {b.index}\n"
        f"timestamp(UTC): {b.timestamp}\n"
        f"difficulty:     {b.difficulty}\n"
        f"nonce:          {b.nonce}\n"
        f"previous_hash:  {b.previous_hash}\n"
        f"hash:           {b.hash}\n"
        f"data:           {b.data}\n"
        "------------------------------",
        flush=True
    )


# ----------------------------
# Flask API (rank 0)
# ----------------------------

def normalize_crowding_event(payload: dict) -> dict:
    store_id = payload.get("store_id") or payload.get("storeId") or payload.get("shop_id")
    ts = payload.get("timestamp") or payload.get("ts") or payload.get("time")
    people_count = payload.get("people_count") or payload.get("peopleCount")

    if ts is None:
        raise ValueError("Missing timestamp/ts/time")
    if people_count is None:
        raise ValueError("Missing people_count/peopleCount")

    try:
        people_count = int(people_count)
    except Exception:
        raise ValueError("people_count must be an integer")

    try:
        _ = iso_to_dt(ts)
    except Exception:
        raise ValueError("timestamp must be ISO8601 (e.g. 2026-01-06T12:34:56Z)")

    normalized = {
        "schema": "store_crowding_exception",
        "schema_version": 1,
        "event_type": "STORE_CROWDING",
        "store_id": store_id,
        "event_timestamp": ts,
        "people_count": people_count,
        "received_at_utc": dt_to_iso(utcnow()),
    }
    return normalized


def start_flask_api(
    host: str,
    port: int,
    comm: MPI.Comm,
    rank: int,
    world_size: int,
    blockchain: Blockchain,
    chain_lock: threading.Lock,
    shared_target: dict,
    event_queue: deque,
    queue_lock: threading.Lock,
    events_by_index: Dict[int, str],
    events_lock: threading.Lock,
    global_stop: threading.Event,
):
    app = Flask(__name__)

    @app.get("/health")
    def health():
        return jsonify({"ok": True})

    @app.get("/status")
    def status():
        with chain_lock:
            latest = blockchain.get_latest_block()
            length = len(blockchain.get_chain())
            valid = blockchain.validate_chain(blockchain.get_chain())
        with queue_lock:
            qlen = len(event_queue)
        with events_lock:
            staged = len(events_by_index)
        return jsonify({
            "rank": rank,
            "world_size": world_size,
            "valid_chain": valid,
            "chain_length": length,
            "latest_index": latest.index,
            "target_index": shared_target["value"],
            "queued_events": qlen,
            "staged_events_by_index": staged,
            "stopping": global_stop.is_set(),
        })

    @app.get("/chain")
    def chain():
        with chain_lock:
            chain_json = blockchain.to_json()
        return jsonify(json.loads(chain_json))

    @app.post("/event")
    def event():
        payload = request.get_json(silent=True)
        if payload is None or not isinstance(payload, dict):
            return jsonify({"ok": False, "error": "Body must be a JSON object"}), 400

        try:
            normalized = normalize_crowding_event(payload)
        except ValueError as e:
            return jsonify({"ok": False, "error": str(e)}), 400

        # event_id based on normalized (without event_id)
        base_str = canonical_json(normalized)
        event_id = sha256_hex(base_str)

        normalized["event_id"] = event_id
        data_str = canonical_json(normalized)

        with queue_lock:
            event_queue.append(data_str)

        shared_target["value"] += 1
        new_target = shared_target["value"]
        broadcast_target_to_all(comm, new_target, rank, world_size)

        return jsonify({
            "ok": True,
            "event_id": event_id,
            "queued": True,
            "target_index": new_target,
        })

    @app.post("/stop")
    def stop():
        global_stop.set()
        broadcast_stop_to_all(comm, rank, world_size)
        return jsonify({"ok": True, "stopping": True})

    def run():
        app.run(host=host, port=port, debug=False, use_reloader=False, threaded=True)

    t = threading.Thread(target=run, daemon=True)
    t.start()
    return app


# ----------------------------
# Main
# ----------------------------

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--threads", type=int, default=0, help="Št. niti na vozlišče (0 => os.cpu_count()).")
    parser.add_argument("--difficulty", type=int, default=5, help="Začetna težavnost (genesis).")
    parser.add_argument("--gen-interval", type=int, default=10, help="Ciljni čas bloka (sek).")
    parser.add_argument("--adj-interval", type=int, default=10, help="Interval prilagoditve težavnosti (v blokih).")
    parser.add_argument("--api-host", type=str, default="0.0.0.0", help="Flask host (rank0).")
    parser.add_argument("--api-port", type=int, default=8000, help="Flask port (rank0).")
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

    # rudarimo samo, ko pride dogodek -> target start = 0 (genesis)
    shared_target = {"value": 0}
    global_stop = threading.Event()

    # rank0: queue dogodkov; vsi ranki: index->data
    event_queue = deque()
    queue_lock = threading.Lock()

    events_by_index: Dict[int, str] = {}
    events_lock = threading.Lock()

    if rank == 0:
        t0 = time.perf_counter()
        start_flask_api(
            host=args.api_host,
            port=args.api_port,
            comm=comm,
            rank=rank,
            world_size=world_size,
            blockchain=blockchain,
            chain_lock=chain_lock,
            shared_target=shared_target,
            event_queue=event_queue,
            queue_lock=queue_lock,
            events_by_index=events_by_index,
            events_lock=events_lock,
            global_stop=global_stop,
        )
        print(f"[rank0] Flask API running on http://{args.api_host}:{args.api_port}", flush=True)
        print("[rank0] Mining happens ONLY when /event is received.", flush=True)

    while not global_stop.is_set():
        mining_stop = threading.Event()

        def listener():
            try:
                while not mining_stop.is_set() and not global_stop.is_set():
                    poll_incoming_messages(
                        comm=comm,
                        blockchain=blockchain,
                        chain_lock=chain_lock,
                        mining_stop=mining_stop,
                        shared_target=shared_target,
                        events_by_index=events_by_index,
                        events_lock=events_lock,
                        global_stop=global_stop,
                        rank=rank
                    )
                    time.sleep(0.005)
            except Exception as e:
                print(f"[rank {rank}] listener crashed: {e}", flush=True)
                mining_stop.set()

        listener_thread = threading.Thread(target=listener, daemon=True)
        listener_thread.start()

        # rank0: dodeli event data za naslednji index, ko je to potrebno
        if rank == 0:
            with chain_lock:
                current_idx = blockchain.get_latest_block().index
                target_idx = shared_target["value"]
                next_index = current_idx + 1

            if current_idx < target_idx:
                with events_lock:
                    has_data_for_next = next_index in events_by_index

                if not has_data_for_next:
                    with queue_lock:
                        data_str = event_queue.popleft() if event_queue else None

                    if data_str is not None:
                        with events_lock:
                            events_by_index.setdefault(next_index, data_str)
                        broadcast_event_to_all(comm, next_index, data_str, rank, world_size)
                        # tu ne ustavim mininga

        # stanje
        with chain_lock:
            current_idx = blockchain.get_latest_block().index
            target_idx = shared_target["value"]

        if current_idx >= target_idx:
            mining_stop.set()
            listener_thread.join(timeout=0.2)
            time.sleep(0.05)
            continue

        next_index = current_idx + 1
        with events_lock:
            data_str = events_by_index.get(next_index)

        if data_str is None:
            mining_stop.set()
            listener_thread.join(timeout=0.2)
            time.sleep(0.05)
            continue

        with chain_lock:
            latest_for_mining = blockchain.get_latest_block()
            diff = blockchain.adjust_difficulty(latest_for_mining.difficulty)

        # MINE
        new_block = mine_block_parallel(
            latest=latest_for_mining,
            data=data_str,
            difficulty=diff,
            rank=rank,
            world_size=world_size,
            thread_count=thread_count,
            stop_event=mining_stop,
        )

        if global_stop.is_set():
            mining_stop.set()
            listener_thread.join(timeout=0.2)
            break

        # Če smo našli blok: TAKOJ broadcast kandidat + stop ostalim
        if new_block is not None:
            broadcast_block_to_all(comm, new_block, rank, world_size)

            added = False
            with chain_lock:
                cur_latest = blockchain.get_latest_block()
                if cur_latest.index + 1 == new_block.index and cur_latest.hash == new_block.previous_hash:
                    added = blockchain.add_block(new_block)

            if added:
                # rank0 will print either here OR when accepting TAG_BLOCK from others
                if rank == 0:
                    print_block(new_block, rank)

                with chain_lock:
                    chain_json = blockchain.to_json()
                broadcast_chain_to_all(comm, chain_json, rank, world_size)

                # cleanup: odstrani event data za potrjene indexe
                with events_lock:
                    latest_idx_now = new_block.index
                    for k in [k for k in events_by_index.keys() if k <= latest_idx_now]:
                        events_by_index.pop(k, None)

        # stop listener
        mining_stop.set()
        listener_thread.join(timeout=0.2)
        time.sleep(0.001)

    comm.barrier()

    if rank == 0:
        t1 = time.perf_counter()
        with chain_lock:
            final_len = len(blockchain.get_chain())
            final_idx = blockchain.get_latest_block().index
            cumdiff = blockchain.calculate_cumulative_diff(blockchain.get_chain())

        print("\n=== REZULTAT ===")
        print(f"nodes:             {world_size}")
        print(f"threads per node:  {thread_count}")
        print(f"final index:       {final_idx}")
        print(f"chain length:      {final_len}")
        print(f"cumulative diff:   {cumdiff:.2f}")
        print(f"time:              {t1 - t0:.3f} s", flush=True)


if __name__ == "__main__":
    main()
