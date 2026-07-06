#ifndef XL_89F92728_5CF9_4DFD_AF25_C208E71C5E9C
#define XL_89F92728_5CF9_4DFD_AF25_C208E71C5E9C
#include <stdint.h>
#include <stdbool.h>
#ifdef WIN32
#ifdef XL_SDK_EXPORTS
#ifdef __cplusplus
#define XL_API(x) extern "C" __declspec(dllexport) x
#else
#define XL_API(x) __declspec(dllexport) x
#endif
#else
#ifdef __cplusplus
#define XL_API(x) extern "C" __declspec(dllimport) x
#else
#define XL_API(x) __declspec(dllimport) x
#endif
#endif
#else
#ifdef __cplusplus
#define XL_API(x) extern "C" __attribute__((visibility("default"))) x
#else
#define XL_API(x) __attribute__((visibility("default"))) x
#endif
#endif

#define XL_DL_MAX_SESSION_ID_LEN                4096

typedef enum xl_dl_task_state_code_t {
    XL_DL_TASK_STATUS_UNKOWN            = 0,
    XL_DL_TASK_STATUS_START_WAITING     = 3,   
    XL_DL_TASK_STATUS_START_PENDING     = 4,   
    XL_DL_TASK_STATUS_STARTED           = 5,   
    XL_DL_TASK_STATUS_STOP_PENDING      = 6,   
    XL_DL_TASK_STATUS_STOPED            = 7,   
    XL_DL_TASK_STATUS_SUCCEEDED         = 8,   
    XL_DL_TASK_STATUS_FAILED            = 9,  
} xl_dl_task_status;

typedef enum xl_dl_task_token_err_t {
    XL_DL_TASK_TOKEN_NORMAL = 0,        
    XL_DL_TASK_TOKEN_EXPIRED,           
    XL_DL_TASK_TOKEN_SESSION_EXPIRED,   
    XL_DL_TASK_TOKEN_OTHERS,            
} xl_dl_task_token_err;

typedef struct xl_dl_create_p2sp_info_t {
    const char* save_path;              
    const char* save_name;              
    const char* url;                    
} xl_dl_create_p2sp_info;

typedef struct xl_dl_file_item_t {
    const char* url;                    // 任务url
    const char* save_path;               // 文件存储路径
    const char* save_name;               // 文件名
    const char* file_hash;               // 文件哈希
} xl_dl_file_item;

typedef struct xl_dl_files_info_t {
    uint32_t file_count; // 文件数量
    xl_dl_file_item* file_list; // 文件列表
} xl_dl_files_info;

typedef struct xl_dl_create_batch_info_t {
    const char* task_name; // 任务名称
    uint32_t max_concurrent; // 最大并发数，默认20
    xl_dl_files_info* batch_files;
} xl_dl_create_batch_info;

typedef struct xl_dl_init_param_t
{
    const char* app_id;                                  
    const char* app_version;             
    const char* cfg_path;                
    uint8_t     save_tasks;              
} xl_dl_init_param;

typedef enum xl_dl_error_t
{
    XL_DL_ERROR_SUCCESS                         = 0,
    XL_DL_ERROR_FAILED                          = 1,
    XL_DL_ERROR_ALREADY_INIT                    = 9101,
    XL_DL_ERROR_SDK_NOT_INIT                    = 9102,
    XL_DL_ERROR_TASK_ALREADY_EXIST              = 9103,
    XL_DL_ERROR_TASK_NOT_EXIST                  = 9104,
    XL_DL_ERROR_TASK_ALREADY_STOPPED            = 9105,
    XL_DL_ERROR_TASK_ALREADY_RUNNING            = 9106,
    XL_DL_ERROR_TASK_NOT_START                  = 9107,
    XL_DL_ERROR_TASK_STILL_RUNNING              = 9108,
    XL_DL_ERROR_FILE_EXISTED                    = 9109,
    XL_DL_ERROR_DISK_FULL                       = 9110,
    XL_DL_ERROR_TOO_MUCH_TASK                   = 9111,
    XL_DL_ERROR_PARAM_ERROR                     = 9112,
    XL_DL_ERROR_SCHEMA_NOT_SUPPORT              = 9113,
    XL_DL_ERROR_DYNAMIC_PARAM_FAIL              = 9114,
    XL_DL_ERROR_CONTINUE_NO_NAME                = 9115,
    XL_DL_ERROR_APPNAME_APPKEY_ERROR            = 9116,
    XL_DL_ERROR_CREATE_THREAD_ERROR             = 9117,
    XL_DL_ERROR_TASK_FINISH                     = 9118,
    XL_DL_ERROR_TASK_NOT_RUNNING                = 9119,
    XL_DL_ERROR_TASK_NOT_IDLE                   = 9120,
    XL_DL_ERROR_TASK_TYPE_NOT_SUPPORT           = 9121,
    XL_DL_ERROR_ADD_RESOURCE_ERROR              = 9122,
    XL_DL_ERROR_FUNCTION_NOT_SUPPORT            = 9123,
    XL_DL_ERROR_ALREADY_HAS_FILENAME            = 9124,
    XL_DL_ERROR_FILE_NAME_TOO_LONG              = 9125,
    XL_DL_ERROR_ONE_PATH_LEVEL_NAME_TOO_LONG    = 9126,
    XL_DL_ERROR_FULL_PATH_NAME_TOO_LONG         = 9127,
    XL_DL_ERROR_FULL_PATH_NAME_OCCUPIED         = 9128,
    XL_DL_ERROR_TASK_NO_FILE_NAME               = 9129,
    XL_DL_ERROR_NOT_WIFI_MODE                   = 9130,
    XL_DL_ERROR_SPEED_LIMIT_TO_SMALL            = 9131,
    XL_DL_ERROR_TASK_CONTROL_STRATEGY           = 9501,
    XL_DL_ERROR_URL_IS_TOO_LONG                 = 9502,
    XL_DL_ERROR_FILE_DELETE_FAIL                = 9503,
    XL_DL_ERROR_FILE_NOT_EXIST                  = 9504,
    XL_DL_ERROR_INFO_NAME_NOT_SUPPORT           = 9505,
    XL_DL_ERROR_MEMORY_TOO_SMALL                = 9601,
    XL_DL_ERROR_AUTH_TOKEN_VERIFY_FAILED        = 9602,
    XL_DL_ERROR_AUTH_SCOPE_VERIFY_FAILED        = 9603,
    XL_DL_ERROR_AUTH_SESSION_ID_VERIFY_FAILED   = 9604,
    XL_DL_ERROR_AUTH_SESSION_ID_EXPIRED         = 9605,
    XL_DL_ERROR_AUTH_RES_HAS_NO_QUOTA           = 9606,
    XL_DL_ERROR_INSUFFICIENT_DISK_SPACE         = 111085,
    XL_DL_ERROR_OPEN_FILE_ERR                   = 111128,
    XL_DL_ERROR_NO_DATA_PIPE                    = 111136,
    XL_DL_ERROR_RESTRICTION                     = 111151,
    XL_DL_ERROR_ACCOUNT_EXCEPTION               = 111152,
    XL_DL_ERROR_RESTRICTION_AREA                = 111153,
    XL_DL_ERROR_COPYRIGHT_BLOCKING              = 111154,
    XL_DL_ERROR_TYPE2_BLOCKING                  = 111155,
    XL_DL_ERROR_TYPE3_BLOCKING                  = 111156,
    XL_DL_ERROR_LONG_TIME_NO_RECV_DATA          = 111176,
    XL_DL_ERROR_TIME_OUT                        = 119212,

    XL_DL_ERROR_TASK_STATUS_ERR                 = 999999,
} xl_dl_error;

typedef struct xl_dl_task_traffic_info_t {
    uint64_t origin_size;
    uint64_t p2p_size;
    uint64_t p2s_size;
    uint64_t dcdn_size;  
} xl_dl_task_traffic_info;

typedef struct xl_dl_task_state_t {
    uint64_t speed;
    uint64_t total_size;
    uint64_t downloaded_size;
    uint8_t  state_code;
    uint32_t task_err_code;
    uint32_t task_token_err;
} xl_dl_task_state;

XL_API(int32_t) xl_dl_init(const xl_dl_init_param* param);
XL_API(int32_t) xl_dl_uninit();

XL_API(int32_t) xl_dl_login(const char* login_token, char* session_id);

XL_API(int32_t) xl_dl_get_unfinished_tasks(uint64_t* task_id_array, uint32_t* count);
XL_API(int32_t) xl_dl_get_finished_tasks(uint64_t* task_id_array, uint32_t* count);


XL_API(int32_t) xl_dl_create_p2sp_task(const xl_dl_create_p2sp_info* create_info, uint64_t* task_id);
XL_API(int32_t) xl_dl_create_batch_task(const xl_dl_create_batch_info* create_info, uint64_t* task_id);

XL_API(int32_t) xl_dl_set_task_token(uint64_t task_id, const char* task_token);

XL_API(int32_t) xl_dl_start_task(uint64_t task_id);
XL_API(int32_t) xl_dl_stop_task(uint64_t task_id);
XL_API(int32_t) xl_dl_delete_task(uint64_t task_id, uint8_t delete_file);


XL_API(int32_t) xl_dl_get_task_state(uint64_t task_id, xl_dl_task_state* state);

//supported info_name: "url", "save_path", "save_name", "traffic"
XL_API(int32_t) xl_dl_get_task_info(uint64_t task_id, const char* info_name, void* buff, uint32_t* buff_len);

XL_API(int32_t) xl_dl_set_concurrent_task_count(uint32_t count);
XL_API(int32_t) xl_dl_set_download_speed_limit(uint32_t speed);
XL_API(int32_t) xl_dl_set_upload_switch(uint32_t upload_switch);
XL_API(int32_t) xl_dl_set_upload_speed_limit(uint32_t speed);
XL_API(int32_t) xl_dl_set_http_header(uint64_t task_id, const char* header_name, const char* header_value);

XL_API(int32_t) xl_dl_version(char* buff, uint32_t* buff_len);

XL_API(int32_t) xl_dl_set_download_url_acceleration(bool enable);

#endif
