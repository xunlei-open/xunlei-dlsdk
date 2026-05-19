#pragma once

#include <exception>
#include <sstream>
#include <string>
#include <vector>

#include <xl_dl/detail/http.h>
#include <xl_dl/detail/json.hpp>

namespace xl_dl {

struct login_token_result {
    int code;
    std::string token;
    int expires_in;
    std::string message;
};

inline std::string build_login_token_body(int expires_in, const std::vector<std::string>& scopes) {
    nlohmann::json body = nlohmann::json::object();
    if (expires_in > 0) {
        body["expires_in"] = expires_in;
    }
    if (!scopes.empty()) {
        body["scopes"] = scopes;
    }
    return body.dump();
}

inline login_token_result get_login_token(
        const std::string& api_key,
        int expires_in,
        const std::vector<std::string>& scopes) {
    std::string body = build_login_token_body(expires_in, scopes);
    login_token_result result;
    result.code = -1;
    result.expires_in = 0;

    std::vector<std::string> headers;
    headers.push_back("Content-Type: application/json");
    headers.push_back("x-api-key: " + api_key);

    std::string response;
    int http_code = detail::http_post(
            "https://open.xunlei.com/api/v1/sdk/login_token",
            body,
            headers,
            response);
    if (http_code != CURLE_OK) {
        result.message = curl_easy_strerror(static_cast<CURLcode>(http_code));
        return result;
    }

    try {
        nlohmann::json json = nlohmann::json::parse(response);
        result.code = json.value("code", -1);
        result.message = json.value("message", "");
        if (json.contains("data") && json["data"].is_object()) {
            result.token = json["data"].value("token", "");
            result.expires_in = json["data"].value("expires_in", 0);
        }
    } catch (const std::exception& e) {
        result.code = -1;
        result.message = e.what();
    }

    return result;
}

inline login_token_result get_login_token(const std::string& api_key) {
    return get_login_token(api_key, 0, std::vector<std::string>());
}

}  // namespace xl_dl
