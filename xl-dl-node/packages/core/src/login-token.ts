const loginTokenUrl = new URL("https://open.xunlei.com/api/v1/sdk/login_token");

export type LoginTokenOptions = {
  expiresIn?: number;
  scopes?: string[];
};

export type LoginTokenResult = {
  code: number;
  token: string;
  expiresIn: number;
  message: string;
};

type LoginTokenResponse = {
  code?: number;
  data?: {
    token?: string;
    expires_in?: number;
  };
  message?: string;
};

export async function getLoginToken(apiKey: string, options: LoginTokenOptions = {}): Promise<LoginTokenResult> {
  if (!apiKey) {
    throw new Error("apiKey 不能为空");
  }

  const body = buildRequestBody(options);
  const response = await fetch(loginTokenUrl, {
    method: "POST",
    headers: {
      "content-type": "application/json",
      "x-api-key": apiKey
    },
    body
  });

  const responseText = await response.text();
  if (!response.ok) {
    throw new Error(`获取 loginToken 失败，HTTP ${response.status}：${responseText}`);
  }

  const payload = JSON.parse(responseText) as LoginTokenResponse;
  return {
    code: payload.code ?? -1,
    token: payload.data?.token ?? "",
    expiresIn: payload.data?.expires_in ?? 0,
    message: payload.message ?? ""
  };
}

function buildRequestBody(options: LoginTokenOptions): string {
  const payload: Record<string, unknown> = {};
  if (options.expiresIn !== undefined) {
    payload.expires_in = options.expiresIn;
  }
  if (options.scopes !== undefined) {
    payload.scopes = options.scopes;
  }
  return JSON.stringify(payload);
}
