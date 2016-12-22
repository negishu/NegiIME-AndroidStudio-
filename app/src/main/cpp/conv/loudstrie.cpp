#include "loudstrie.h"

loudstrie::loudstrie() :NumOfItems_(0), EdgeCharacter_(NULL)
{
}

loudstrie::~loudstrie(void)
{
}

void loudstrie::Open(const U08* data)
{
    const U32 trie_size = Read32(data);
    const U32 terminal_size = Read32(data + 4);
    const U32 num_edge_characters = Read32(data + 8);
    const U32 num_items = Read32(data + 12);

    const U08 *trie_image = data + 16;
    const U08 *terminal_image = trie_image + trie_size;
    const U08 *edge_character = terminal_image + terminal_size;

    Louds_.Open(trie_image, trie_size);

    BitVector_.Init(terminal_image, terminal_size);

    EdgeCharacter_ = reinterpret_cast<const U08*>(edge_character);

    NumOfItems_ = num_items;
}

const U08* loudstrie::Reverse(const U32 uID, U08 *buffer, U08& length) const
{
    if (uID >= NumOfItems_) {

        length = 0; return NULL;
    }

    U32 node_id = BitVector_.Select1(uID) + 1;

    U08 *ptr = buffer + length;

    *ptr = '\0'; length = 0;

    while (node_id > 1) {

        --ptr; *ptr = EdgeCharacter_[node_id - 1]; length++;

        const int bit_index = GetParentEdgeBitIndex(node_id - 1);

        node_id = GetParentNodeId(bit_index);
    }
    return ptr;
}

const U08* loudstrie::ReverseForKey(const U32 uID, U08 *buffer, U08& length) const
{
    if (uID >= NumOfItems_) {

        length = 0; return NULL;
    }

    int node_id = BitVector_.Select1(uID) + 1;

    U08 *ptr = (U08 *)buffer + length;

    *ptr = '\0'; length = 0;

    while (node_id > 1) {

        --ptr; *ptr = EdgeCharacter_[node_id - 1] & 0x7F; length++;

        const int bit_index = GetParentEdgeBitIndex(node_id - 1);

        node_id = GetParentNodeId(bit_index);
    }
    return ptr;
}
