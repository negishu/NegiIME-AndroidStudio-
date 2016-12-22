#include "Search.h"
#include ".\\utils.h"

#include <stdlib.h>

Search::Search(const Dictionary& worddictionary, const Dictionary& suffixdictionary) : worddictionary_(worddictionary), suffixdictionary_(suffixdictionary)
{
}

Search::~Search(void)
{

}

bool Search::DoSearch(searcher* psearcher, const U08 nPos, std::string& Key, std::string& Word, std::vector<searcher::Candidate>& candidate)
{
    if (psearcher) {
        psearcher->Search(nPos, Key, Word, worddictionary_, suffixdictionary_, candidate);
    }
    return true;
}
