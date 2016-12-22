#pragma once

#include <map>

#include "./global_utils.h"
#include "./utils.h"

#include "louds.h"
#include "bitvector.h"

class loudstrie
{
public:
    loudstrie();
    virtual ~loudstrie(void);
    void Open(const U08* data);
    const U08* Reverse(const U32 uID, U08 *buffer, U08& length) const;
    const U08* ReverseForKey(const U32 uID, U08 *buffer, U08& length) const;
    inline const U32 GetChildNodeId(const U32 bit_index) const
    {
        return Louds_.GetChildNodeId(bit_index);
    }
    inline const U08 IsOriginalEdgeBit(const U32 bit_index) const
    {
        return Louds_.IsOriginalEdgeBit(bit_index);
    }
    inline const U08 IsEdgeBit(const U32 bit_index) const
    {
        return Louds_.IsEdgeBit(bit_index);
    }
    inline const U08 IsTerminalNode(const U32 node_id) const
    {
        return BitVector_.Get(node_id);
    }
    inline const U08 GetEdgeChar(const U32 node_id) const
    {
        return (EdgeCharacter_[node_id]);
    }
    inline const U32 GetNodeNumber(const U32 node_id) const
    {
        return BitVector_.Rank1(node_id);
    }
    inline const U32 GetFirstEdgeBitIndex(const U32 node_id) const
    {
        return Louds_.GetFirstEdgeBitIndex(node_id);
    }
    inline const U32 GetParentNodeId(const U32 bit_index) const
    {
        return Louds_.GetParentNodeId(bit_index);
    }
    inline const U32 GetParentEdgeBitIndex(const U32 node_id) const
    {
        return Louds_.GetParentEdgeBitIndex(node_id);
    }
    inline const U32 GetItemCount() const
    {
        return NumOfItems_;
    }
    static const U32 kRootIndex = 2;
private:
    louds       Louds_;
    bitvector   BitVector_;
    const U08 * EdgeCharacter_;
    U32         NumOfItems_;
};
