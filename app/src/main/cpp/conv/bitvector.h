#pragma once

#include "./global_utils.h"
#include "./utils.h"

class bitvector {
public:
    bitvector();
    ~bitvector();
    void  Init(const U08 *data, U32 bytes);
    const U32 Get(U32 pos) const;
    const U32 Select0(U32 n) const;
    const U32 Select1(U32 n) const;
    const U32 Rank0(U32 pos) const;
    const U32 Rank1(U32 pos) const;
private:
    static const U32 BSIZE = 32;
    static const U32 LEVELA = (1 << 8);
    static const U32 LEVELB = (1 << 5);
    const U32 _Rank(U32 pos) const;
    const U32 GetTotal0(U32 i) const;
    const U32 FindChunk0(U32 n) const;
    const U32 GetTotal1(U32 i) const;
    const U32 FindChunk1(U32 n) const;
    const U32 SelectNaive0(U32 start, U32 n) const;
    const U32 SelectNaive1(U32 start, U32 n) const;
    const U32 count_bits_in_chunk(U32 c, U32 limit);
    const U32 get_index_len(U32 n);
    U32* B;
    U32 n_bites;
    U32 n_bytes;
    U32* levelA;
    U08* levelB;
    U32 levelA_size;
    U32 levelB_size;
    U32 *index;
    U32 index_len;
};
