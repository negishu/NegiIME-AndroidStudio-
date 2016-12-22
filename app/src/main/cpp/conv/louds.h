#pragma once

#include "./global_utils.h"
#include "./utils.h"

#include "bitvector.h"

class louds
{
public:
    louds();
    virtual ~louds(void);
    void Open(const U08* data, const U32 size);
    const bool IsOriginalEdgeBit(const U32 bit_index) const;
    const bool IsEdgeBit(const U32 bit_index) const;
    const U32 GetChildNodeId(const U32 bit_index) const;
    const U32 GetFirstEdgeBitIndex(const U32 node_id) const;
    const U32 GetParentNodeId(const U32 bit_index) const;
    const U32 GetParentEdgeBitIndex(const U32 node_id) const;
    static inline const int GetChildNodeId(const U32 node_id, const U32 bit_index) {
        return bit_index - node_id + 1;
    }
private:

    static const U32 BSIZE = 32;

    bitvector BitVector_;

    U32 *B_, SIZE_;
};
