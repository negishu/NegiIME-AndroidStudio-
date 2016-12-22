#include "Converter.h"

#include "Dictionary.h"
#include "wordDictionary.h"
#include "suffixDictionary.h"
#include "Search.h"
#include "NBestGenerator.h"
#include "Lattice.h"
#include "Connector.h"

#include <iostream>

Converter::Converter(void)
{
    _pdictionary.reset(new wordDictionary);
    _psuffixdictionary.reset(new suffixDictionary);
    _pconnector.reset(new Connector);
    _pnbestGenerator.reset(new NBestGenerator(*_pconnector));
    _pSearch.reset(new Search(*_pdictionary, *_psuffixdictionary));
}

Converter::~Converter(void)
{
}

const double Converter::GetTransitionCost(const Node *lnode, const Node *rnode) const
{
    return _pconnector->GetTransitionCost(lnode->rid, rnode->lid);
}

bool Converter::Viterbi(const Lattice &lattice) const
{
    const std::string &key = lattice.key();

    for (size_t pos = 0; pos <= key.size(); ++pos) {

        for (Node *rnode = (Node *)lattice.begin_nodes(pos); rnode != NULL; rnode = rnode->bnext) {

            double best_cost = INT_MAX;

            Node *best_node = NULL;

            for (Node *lnode = (Node *)lattice.end_nodes(pos); lnode != NULL; lnode = lnode->enext) {

                double cost = lnode->tcost + GetTransitionCost(lnode, rnode) + rnode->cost;

                if (cost < best_cost) {

                    best_node = lnode;
                    best_cost = cost;
                }
            }

            rnode->prev = best_node;
            rnode->tcost = best_cost;
        }
    }

    Node *node = (Node *)lattice.eos_nodes();

    Node *prev = NULL;

    while (node->prev != NULL && node != node->prev) {

        prev = node->prev;
        prev->next = node;

        node = prev;
    }

    if (lattice.bos_nodes() != prev) {

        return false;
    }

    return true;
}

const int Converter::Convert(const std::string& key)
{
    Lattice lattice;

    lattice.SetKey(key);

    std::string cur_key = lattice.key();

    std::string word;

    static searcher asearcher;

    std::vector<searcher::Candidate> candidate;

    const char *key_begin = cur_key.data();
    const char *key_end = cur_key.data() + cur_key.size();

    for (size_t pos = 0; pos < cur_key.size(); ++pos) {

        if (lattice.end_nodes(pos) != NULL)
        {
            std::string key(key_begin + pos);

            candidate.clear();

            _pSearch->DoSearch(&asearcher, pos, key, word, candidate);

            lattice.Insert(pos, candidate);
        }
    }

    _result[0] = 0;

    if (Viterbi(lattice) == false) {

        return 0;
    }

    _pnbestGenerator->Init(lattice.bos_nodes(), lattice.eos_nodes(), &lattice);

    {
        Candidate new_candidate;

        candidates_.clear();

        for (int i = 0, ii = 0; i < 32; i++) {

            new_candidate.Init();

            if (_pnbestGenerator->Next(new_candidate) == false) {
                _result[ii] = 0;
                break;
            }

            bool bFound = true;

            const Candidate& new_cand = new_candidate;

            if (candidates_.empty()) {

                bFound = false;
            }
            else {

                bFound = false;

                for (std::vector<Candidate>::const_iterator it = candidates_.begin(); it != candidates_.end(); ++it) {

                    const Candidate& save_cand = *it;

                    if (save_cand.word == new_cand.word) {

                        bFound = true;
                        break;
                    }
                }
            }

            if (bFound == false) {

                candidates_.push_back(new_candidate);
            }
        }
    }

    return candidates_.size();
}

bool Converter::ConvertForNext(const std::string& key, const std::string& word)
{
    Lattice lattice;
    candidates_.clear();

    std::string KEY = key;
    std::string WORD = word;

    if (KEY.length() > 0) {

        lattice.SetKey(KEY);

        static searcher asearcher;

        std::vector<searcher::Candidate> candidate;

        for (size_t pos = 0; pos < KEY.size(); ++pos) {

            if (lattice.end_nodes(pos) != NULL)
            {
                candidate.clear();

                _pSearch->DoSearch(&asearcher, pos, KEY, WORD, candidate);

                lattice.Insert(pos, candidate);
            }
        }
    }
    else {

        static searcher asearcher;

        std::vector<searcher::Candidate> candidate;

        _pSearch->DoSearch(&asearcher, 0, KEY, WORD, candidate);

        lattice.SetKey(KEY);

        lattice.Insert(0, candidate);
    }

    _result[0] = 0;

    if (Viterbi(lattice) == false) {

        return false;
    }

    _pnbestGenerator->Init(lattice.bos_nodes(), lattice.eos_nodes(), &lattice);

    {
        Candidate new_candidate;

        for (int i = 0, ii = 0; i < 32; i++) {

            new_candidate.Init();

            if (_pnbestGenerator->Next(new_candidate) == false) {
                _result[ii] = 0;
                break;
            }

            bool bFound = true;

            const Candidate& new_cand = new_candidate;

            if (candidates_.empty()) {

                bFound = false;
            }
            else {

                bFound = false;

                for (std::vector<Candidate>::const_iterator it = candidates_.begin(); it != candidates_.end(); ++it) {
                    const Candidate& save_cand = *it;
                    if (save_cand.word == new_cand.word) {
                        bFound = true;
                        break;
                    }
                }
            }

            if (bFound == false) {

                candidates_.push_back(new_candidate);
            }
        }
    }

    return true;
}

const int Converter::GetCandidate(std::string& ret, const int nIndex)
{
    ret.clear();

    if (nIndex < candidates_.size()) {

        ret = candidates_[nIndex].word;
    }

    return ret.length();
}

const Candidate& Converter::GetCandidate(const int nIndex)
{
    if (nIndex < candidates_.size()) {

        return candidates_[nIndex];
    }

    static Candidate empty;

    return empty;
}