#pragma once

#include <string>
#include <vector>

#include <curl/curl.h>

namespace xl_dl {
namespace detail {

inline size_t http_data_callback(void* contents, size_t size, size_t nmemb, std::string* response) {
    size_t total_size = size * nmemb;
    response->append(static_cast<char*>(contents), total_size);
    return total_size;
}

inline int http_post(
        const std::string& url,
        const std::string& post_data,
        const std::vector<std::string>& headers,
        std::string& response) {
    CURLcode result = CURLE_FAILED_INIT;
    CURL* curl = NULL;
    curl_slist* header_list = NULL;

    curl_global_init(CURL_GLOBAL_ALL);

    for (std::vector<std::string>::const_iterator it = headers.begin(); it != headers.end(); ++it) {
        header_list = curl_slist_append(header_list, it->c_str());
    }

    curl = curl_easy_init();
    if (curl != NULL) {
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_POST, 1L);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, post_data.c_str());
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, header_list);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, http_data_callback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);

        result = curl_easy_perform(curl);
        curl_easy_cleanup(curl);
    }

    if (header_list != NULL) {
        curl_slist_free_all(header_list);
    }

    curl_global_cleanup();
    return static_cast<int>(result);
}

inline int http_post(const std::string& url, const std::string& post_data, std::string& response) {
    std::vector<std::string> headers;
    headers.push_back("Content-Type: application/json");
    return http_post(url, post_data, headers, response);
}

}  // namespace detail
}  // namespace xl_dl
