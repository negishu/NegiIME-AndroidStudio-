#include <jni.h>
#include <iostream>
#include <string>
#include <string.h>
#include <stdlib.h>

#include "NegiIME.h"
#include "message.pb.h"

#include <android/log.h>

#include ".\\conv\\converter.h"
#include ".\\conv\\utils.h"

NegiIME::NegiIME()
{
	_pConverter = new Converter();
}

NegiIME::~NegiIME()
{

}

const int NegiIME::ProcessData(const char* pInBuf, const int nInLen, char* pOutBuf, const int nOutLen)
{
	negi::android::NDK::SET::CANDS inMyCands;
	inMyCands.ParseFromArray(pInBuf, nInLen);

	negi::android::NDK::SET::CANDS outMyCands;
	outMyCands.clear_candidate();

	for (int i = 0; i < inMyCands.candidate_size(); i++) {
		if (i < 8) {
			const negi::android::NDK::SET::CANDS_CAND cand = inMyCands.candidate(i);
			_wstring winput;
			_wstring wromajiinput;
			Utils::UTF8ToUSC2(cand.context(), wromajiinput);
			Utils::RomajiToHiragana(wromajiinput, winput);
			std::string yomi, out;
			Utils::USC2ToValue(winput, yomi);
			if (yomi.length() > 0) {
				__android_log_print(ANDROID_LOG_VERBOSE, "TAG", "yomi = %s", yomi.c_str());
				if (_pConverter) {
					__android_log_print(ANDROID_LOG_VERBOSE, "TAG", "_pConverter->Convert(yomi)");
					_pConverter->Convert(yomi);
				}
			}
			else {
				break;
			}

			for (int n = 0; n < 24; n++) {
				std::string ret;
				int len = _pConverter->GetCandidate(ret,n);
				__android_log_print(ANDROID_LOG_VERBOSE, "TAG", "len = %d", len);
				if (len > 0) {
					std::string out; Utils::SJISToUTF8(ret, out);
					__android_log_print(ANDROID_LOG_VERBOSE, "TAG", "out = %s", out.c_str());
					negi::android::NDK::SET::CANDS_CAND* addone = outMyCands.add_candidate();
					addone->set_context(out);
				}
				else {
					break;
				}
			}
		}
	}

	int nSize = outMyCands.ByteSize();
	outMyCands.SerializeToArray(pOutBuf, nSize);
	return nSize;
}
