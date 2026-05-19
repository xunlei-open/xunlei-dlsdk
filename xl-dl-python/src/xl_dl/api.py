import ctypes
import json
from dataclasses import dataclass
from typing import List, Optional, Tuple
from urllib.request import Request, urlopen

from .loader import load_library

ERROR_SUCCESS = 0
ERROR_ALREADY_INIT = 9101

TASK_STATUS_UNKNOWN = 0
TASK_STATUS_START_WAITING = 3
TASK_STATUS_START_PENDING = 4
TASK_STATUS_STARTED = 5
TASK_STATUS_STOP_PENDING = 6
TASK_STATUS_STOPPED = 7
TASK_STATUS_SUCCEEDED = 8
TASK_STATUS_FAILED = 9

MAX_SESSION_ID_LEN = 4096
LOGIN_TOKEN_URL = "https://open.xunlei.com/api/v1/sdk/login_token"


class _InitParam(ctypes.Structure):
    _fields_ = [
        ("app_id", ctypes.c_char_p),
        ("app_version", ctypes.c_char_p),
        ("cfg_path", ctypes.c_char_p),
        ("save_tasks", ctypes.c_uint8),
    ]


class _CreateP2spInfo(ctypes.Structure):
    _fields_ = [
        ("save_path", ctypes.c_char_p),
        ("save_name", ctypes.c_char_p),
        ("url", ctypes.c_char_p),
    ]


class _TaskState(ctypes.Structure):
    _fields_ = [
        ("speed", ctypes.c_uint64),
        ("total_size", ctypes.c_uint64),
        ("downloaded_size", ctypes.c_uint64),
        ("state_code", ctypes.c_uint8),
        ("task_err_code", ctypes.c_uint32),
        ("task_token_err", ctypes.c_uint32),
    ]


@dataclass
class TaskState:
    speed: int
    total_size: int
    downloaded_size: int
    state_code: int
    task_err_code: int
    task_token_err: int


class XLDownloadAPI:
    def __init__(self):
        self._lib = load_library()
        self._bind()

    def init(self, app_id: str, app_version: str, cfg_path: str, save_tasks: bool = True) -> int:
        param = _InitParam(
            app_id.encode("utf-8"),
            app_version.encode("utf-8"),
            cfg_path.encode("utf-8"),
            1 if save_tasks else 0,
        )
        return self._lib.xl_dl_init(ctypes.byref(param))

    def get_login_token(
        self,
        api_key: str,
        expires_in: Optional[int] = None,
        scopes: Optional[List[str]] = None,
    ) -> Tuple[int, str, int, str]:
        if not api_key:
            raise ValueError("api_key 不能为空")

        payload = {}
        if expires_in is not None:
            payload["expires_in"] = expires_in
        if scopes:
            payload["scopes"] = scopes

        data = json.dumps(payload).encode("utf-8") if payload else None
        headers = {"x-api-key": api_key, "Accept": "application/json"}
        if data is not None:
            headers["Content-Type"] = "application/json"

        request = Request(LOGIN_TOKEN_URL, data=data, headers=headers, method="POST")
        with urlopen(request, timeout=30) as response:
            body = json.loads(response.read().decode("utf-8"))

        token_data = body.get("data") or {}
        return (
            int(body.get("code", -1)),
            token_data.get("token", ""),
            int(token_data.get("expires_in", 0)),
            body.get("message", ""),
        )

    def uninit(self) -> int:
        return self._lib.xl_dl_uninit()

    def version(self) -> Tuple[int, str]:
        length = ctypes.c_uint32(0)
        result = self._lib.xl_dl_version(None, ctypes.byref(length))
        if result != ERROR_SUCCESS:
            return result, ""

        buffer = ctypes.create_string_buffer(length.value + 1)
        result = self._lib.xl_dl_version(buffer, ctypes.byref(length))
        return result, buffer.value.decode("utf-8") if result == ERROR_SUCCESS else ""

    def login(self, login_token: str) -> Tuple[int, str]:
        session = ctypes.create_string_buffer(MAX_SESSION_ID_LEN)
        result = self._lib.xl_dl_login(login_token.encode("utf-8"), session)
        return result, session.value.decode("utf-8") if result == ERROR_SUCCESS else ""

    def create_p2sp_task(self, url: str, save_path: str, save_name: str) -> Tuple[int, int]:
        info = _CreateP2spInfo(
            save_path.encode("utf-8"),
            save_name.encode("utf-8"),
            url.encode("utf-8"),
        )
        task_id = ctypes.c_uint64(0)
        result = self._lib.xl_dl_create_p2sp_task(ctypes.byref(info), ctypes.byref(task_id))
        return result, task_id.value

    def start_task(self, task_id: int) -> int:
        return self._lib.xl_dl_start_task(ctypes.c_uint64(task_id))

    def stop_task(self, task_id: int) -> int:
        return self._lib.xl_dl_stop_task(ctypes.c_uint64(task_id))

    def delete_task(self, task_id: int, delete_file: bool) -> int:
        return self._lib.xl_dl_delete_task(
            ctypes.c_uint64(task_id),
            ctypes.c_uint8(1 if delete_file else 0),
        )

    def get_task_state(self, task_id: int) -> Tuple[int, Optional[TaskState]]:
        state = _TaskState()
        result = self._lib.xl_dl_get_task_state(ctypes.c_uint64(task_id), ctypes.byref(state))
        if result != ERROR_SUCCESS:
            return result, None
        return result, TaskState(
            speed=state.speed,
            total_size=state.total_size,
            downloaded_size=state.downloaded_size,
            state_code=state.state_code,
            task_err_code=state.task_err_code,
            task_token_err=state.task_token_err,
        )

    def _bind(self):
        self._lib.xl_dl_init.argtypes = [ctypes.POINTER(_InitParam)]
        self._lib.xl_dl_init.restype = ctypes.c_int32
        self._lib.xl_dl_uninit.restype = ctypes.c_int32
        self._lib.xl_dl_version.argtypes = [ctypes.c_void_p, ctypes.POINTER(ctypes.c_uint32)]
        self._lib.xl_dl_version.restype = ctypes.c_int32
        self._lib.xl_dl_login.argtypes = [ctypes.c_char_p, ctypes.c_void_p]
        self._lib.xl_dl_login.restype = ctypes.c_int32
        self._lib.xl_dl_create_p2sp_task.argtypes = [
            ctypes.POINTER(_CreateP2spInfo),
            ctypes.POINTER(ctypes.c_uint64),
        ]
        self._lib.xl_dl_create_p2sp_task.restype = ctypes.c_int32
        self._lib.xl_dl_start_task.argtypes = [ctypes.c_uint64]
        self._lib.xl_dl_start_task.restype = ctypes.c_int32
        self._lib.xl_dl_stop_task.argtypes = [ctypes.c_uint64]
        self._lib.xl_dl_stop_task.restype = ctypes.c_int32
        self._lib.xl_dl_delete_task.argtypes = [ctypes.c_uint64, ctypes.c_uint8]
        self._lib.xl_dl_delete_task.restype = ctypes.c_int32
        self._lib.xl_dl_get_task_state.argtypes = [
            ctypes.c_uint64,
            ctypes.POINTER(_TaskState),
        ]
        self._lib.xl_dl_get_task_state.restype = ctypes.c_int32
