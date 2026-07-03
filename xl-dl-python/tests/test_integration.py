import os
import shutil
import sys
import tempfile
import time
import unittest
from pathlib import Path
from urllib.parse import urlparse

from xl_dl import (
    ERROR_ALREADY_INIT,
    ERROR_SUCCESS,
    TASK_STATUS_FAILED,
    TASK_STATUS_SUCCEEDED,
    XLDownloadAPI,
)


APP_VERSION = "1.0"
DEFAULT_TASK_URL = "https://down.sandai.net/thunder11/XunLeiSetup12.0.12.2510.exe"
DEFAULT_TIMEOUT_SECONDS = 900


class XLDownloadAPIIntegrationTest(unittest.TestCase):
    def download_test(self, sdk, task_url, save_path, timeout_seconds):
        save_name = Path(urlparse(task_url).path).name or "download.tmp"
        create_result, task_id = sdk.create_p2sp_task(task_url, str(save_path), save_name)
        self.assertEqual(create_result, ERROR_SUCCESS)
        self.assertGreater(task_id, 0)

        start_result = sdk.start_task(task_id)
        self.assertEqual(start_result, ERROR_SUCCESS)

        deadline = time.monotonic() + timeout_seconds
        while time.monotonic() < deadline:
            state_result, state = sdk.get_task_state(task_id)
            self.assertEqual(state_result, ERROR_SUCCESS)
            self.assertIsNotNone(state)
            if state.state_code == TASK_STATUS_SUCCEEDED:
                return
            self.assertNotEqual(state.state_code, TASK_STATUS_FAILED)
            time.sleep(1)
        
        self.fail(f"task {task_id} did not finish within {timeout_seconds} seconds")


    def test_download_flow(self):
        api_key = os.environ.get("API_KEY")
        self.assertTrue(api_key, "API_KEY environment variable is required")
        app_id = os.environ.get("APP_ID")
        self.assertTrue(app_id, "APP_ID environment variable is required")

        task_url = os.environ.get("XL_DL_TEST_URL") or DEFAULT_TASK_URL
        timeout_seconds = int(os.environ.get("XL_DL_TEST_TIMEOUT_SECONDS", DEFAULT_TIMEOUT_SECONDS))

        sdk = None
        task_id = 0
        config_path = None
        save_path = None
        try:
            config_path = Path(tempfile.mkdtemp(prefix="xl-dl-python-cfg-"))
            save_path = Path(tempfile.mkdtemp(prefix="xl-dl-python-downloads-"))
            sdk = XLDownloadAPI()
            init_result = sdk.init(app_id, APP_VERSION, str(config_path), save_tasks=True)
            self.assertIn(init_result, (ERROR_SUCCESS, ERROR_ALREADY_INIT))

            try:
                code, login_token, _expires_in, message = sdk.get_login_token(api_key)
                if code != 0:
                    print(f"warning: get loginToken failed, code={code}, message={message}", file=sys.stderr)
                elif not login_token:
                    print("warning: loginToken is empty, continue without login", file=sys.stderr)
                else:
                    try:
                        login_result, session_id = sdk.login(login_token)
                        if login_result != ERROR_SUCCESS:
                            print(f"warning: login failed: {login_result}, continue without login", file=sys.stderr)
                        elif not session_id:
                            print("warning: session id is empty, continue without login", file=sys.stderr)
                    except Exception as error:
                        print(f"warning: login failed: {error}, continue without login", file=sys.stderr)
            except Exception as error:
                print(f"warning: get loginToken failed: {error}, continue without login", file=sys.stderr)

            
            self.download_test(sdk, task_url, save_path, timeout_seconds)

            set_result = sdk.set_download_url_acceleration(False)
            self.assertEqual(set_result, ERROR_SUCCESS)

            self.download_test(sdk, task_url, save_path, timeout_seconds)
        finally:
            try:
                if task_id and sdk is not None:
                    sdk.delete_task(task_id, delete_file=True)
                if sdk is not None:
                    sdk.uninit()
            finally:
                if config_path is not None:
                    shutil.rmtree(config_path, ignore_errors=True)
                if save_path is not None:
                    shutil.rmtree(save_path, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
