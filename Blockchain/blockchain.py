from dataclasses import asdict, dataclass
from datetime import datetime, timedelta
import hashlib
import json
from time import timezone
from typing import List


def utcnow() -> datetime:
    return datetime.now(timezone.utc)

def dt_to_iso(dt: datetime) -> str:
    #UTC ISO format
    return dt.astimezone(timezone.utc).isoformat()

def iso_to_dt(s: str) -> datetime:
    return datetime.fromisoformat(s).astimezone(timezone.utc)

def sha256_hex(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest().upper()


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
    

class Blockchain:
    def __init__(self, difficulty: int = 5, block_generation_interval: int = 10, difficulty_adjustment_interval: int = 10):
        self.chain: List[Block] = []
        self.difficulty = difficulty
        self.block_generation_interval = block_generation_interval
        self.difficulty_adjustment_interval = difficulty_adjustment_interval
        self.chain.append(self.create_genesis_block())

    def create_genesis_block(self) -> Block:
        # genesis timestamp = UnixEpoch, prev_hash = "0"
        ts = datetime(1970, 1, 1, tzinfo=timezone.utc)
        timestamp_iso = dt_to_iso(ts)
        nonce, h = 0, Block.compute_hash(0, "Genesis Block", timestamp_iso, "0", self.difficulty, 0)
        #proof of work tudi za genesis?
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
        now = utcnow()
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
        #valid + več kumulativne težavnosti
        if not new_chain:
            return False
        
        if self.calculate_cumulative_diff(new_chain) > self.calculate_cumulative_diff(self.chain) and self.validate_chain(new_chain):

            self.chain = new_chain
            return True
        
        return False

    def to_json(self) -> str:
        return json.dumps([asdict(b) for b in self.chain], separators=(",", ":"))
    
    
    @staticmethod
    def from_json(s: str) -> List[Block]:
        raw = json.loads(s)
        chain = []
        for item in raw:
            chain.append(Block(**item))
        return chain