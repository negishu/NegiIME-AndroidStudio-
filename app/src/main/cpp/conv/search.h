#pragma once

#include "./global_utils.h"
#include "./utils.h"

#include "./Dictionary.h"
#include "./searcher.h"

class Search {
public:
    Search(const Dictionary& worddictionary, const Dictionary& suffixdictionary);
    virtual ~Search(void);
    bool DoSearch(searcher* psearcher, const U08 nPos, std::string& Key, std::string& Word, std::vector<searcher::Candidate>& candidate);
protected:
    const Dictionary& worddictionary_;
    const Dictionary& suffixdictionary_;
};
