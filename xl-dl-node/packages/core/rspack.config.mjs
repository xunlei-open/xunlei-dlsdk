import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default {
  mode: "production",
  target: "node18",
  entry: "./src/index.ts",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "index.cjs",
    library: {
      type: "commonjs2"
    },
    clean: true
  },
  externals: {
    koffi: "commonjs2 koffi"
  },
  resolve: {
    extensions: [".ts", ".js"]
  },
  module: {
    rules: [
      {
        test: /\.ts$/,
        loader: "builtin:swc-loader",
        options: {
          jsc: {
            parser: {
              syntax: "typescript"
            },
            target: "es2022"
          }
        }
      }
    ]
  }
};
