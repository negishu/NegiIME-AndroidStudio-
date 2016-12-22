#include "louds.h"

louds::louds()
{
}

louds::~louds(void)
{
}

void louds::Open(const U08* data, const U32 size)
{
    BitVector_.Init(data, size);

    B_ = (U32*)data; SIZE_ = size;
}

const bool louds::IsOriginalEdgeBit(const U32 bit_index) const
{
    const U32 blockPos = bit_index / BSIZE;
    const U32 offset = bit_index%BSIZE;

    return ((B_[blockPos] >> offset) & 1);
}

const bool louds::IsEdgeBit(const U32 bit_index) const
{
    const U32 blockPos = bit_index / BSIZE;
    const U32 offset = bit_index%BSIZE;

    return ((B_[blockPos] >> offset) & 1);
}

const U32 louds::GetChildNodeId(const U32 bit_index) const
{
    return BitVector_.Rank1(bit_index) + 1;
}

const U32 louds::GetFirstEdgeBitIndex(const U32 node_id) const
{
    return BitVector_.Select0(node_id) + 1;
}

const U32 louds::GetParentNodeId(const U32 bit_index) const
{
    return BitVector_.Rank0(bit_index);
}

const U32 louds::GetParentEdgeBitIndex(const U32 node_id) const
{
    return BitVector_.Select1(node_id);
}
