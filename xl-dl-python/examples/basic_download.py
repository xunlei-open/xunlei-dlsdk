import time
import os
from pathlib import Path
from urllib.parse import urlparse

from xl_dl import ERROR_ALREADY_INIT, ERROR_SUCCESS, TASK_STATUS_FAILED, TASK_STATUS_SUCCEEDED, XLDownloadAPI


APP_ID = "eGwtcVo4SDEwMDMwAAAAAy4nxxx="  # TODO: Replace with your own app ID.
API_KEY = "xl_ba3edc87e2734c8bf177a04f3dd4xxx"  # TODO: Replace with your own API key.
APP_VERSION = "1.0"
TASK_URL = "https://down.sandai.net/thunder11/XunLeiSetup12.0.12.2510.exe"


def main() -> int:
    config_path = "/tmp/xl_dl_sdk_conf"
    save_path = "/tmp/ThunderDownload"
    os.makedirs(config_path, exist_ok=True)
    os.makedirs(save_path, exist_ok=True)

    save_name = Path(urlparse(TASK_URL).path).name
    sdk = XLDownloadAPI()

    result, version = sdk.version()
    print(f"version result:{result} version:{version}")

    result = sdk.init(APP_ID, APP_VERSION, config_path, save_tasks=True)
    print(f"init result:{result}")
    if result not in (ERROR_SUCCESS, ERROR_ALREADY_INIT):
        return result

    try:
        code, login_token, _expires_in, message = sdk.get_login_token(API_KEY)
        if code != 0:
            raise RuntimeError(f"获取 loginToken 失败：code={code}, message={message}")
        result, session_id = sdk.login(login_token)
        print(f"login result:{result} session:{session_id}")
        if result != ERROR_SUCCESS:
            return result

        result, task_id = sdk.create_p2sp_task(TASK_URL, save_path, save_name)
        print(f"create task result:{result} task_id:{task_id}")
        if result != ERROR_SUCCESS:
            return result

        result = sdk.start_task(task_id)
        print(f"start task result:{result}")
        if result != ERROR_SUCCESS:
            return result

        while True:
            result, state = sdk.get_task_state(task_id)
            if state is None:
                print(f"get task state result:{result}")
                break

            print(
                f"\rstate:{state.state_code} downloaded:{state.downloaded_size}/{state.total_size} speed:{state.speed}",
                end="",
                flush=True,
            )

            if state.state_code in (TASK_STATUS_SUCCEEDED, TASK_STATUS_FAILED):
                print()
                break

            time.sleep(1)

        return 0
    finally:
        result = sdk.uninit()
        print(f"uninit result:{result}")


if __name__ == "__main__":
    raise SystemExit(main())
