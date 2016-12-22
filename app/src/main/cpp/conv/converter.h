#pragma once

#include ".\\global_utils.h"
#include ".\\utils.h"
#include ".\\NBestGenerator.h"
#include ".\\searcher.h"

class Dictionary;
class Connector;
class Search;
class Lattice;
class Node;

class Converter
{
public:
    Converter(void);
    virtual ~Converter(void);
    const int Convert(const std::string& key);
    bool ConvertForNext(const std::string& key, const std::string& ret);
    const int GetCandidate(std::string& ret, const int nIndex);
    const Candidate& GetCandidate(const int nIndex);
protected:
    bool Viterbi(const Lattice &lattice) const;
    const double GetTransitionCost(const Node *lnode, const Node *rnode) const;
    scoped_ptr<Dictionary>     _pdictionary;
    scoped_ptr<Dictionary>     _psuffixdictionary;
    scoped_ptr<Connector>      _pconnector;
    scoped_ptr<Search>         _pSearch;
    scoped_ptr<NBestGenerator> _pnbestGenerator;
    char _result[1024];
    std::vector<Candidate> candidates_;
    std::vector<searcher::Candidate> candidate;
};
