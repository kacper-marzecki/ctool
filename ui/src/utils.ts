import Axios from "axios";
import { promises } from "dns";
import { match, __ } from "ts-pattern";

export function add<T>(arr: T[], elem: T): T[] {
  arr.push(elem);
  return arr;
}


export type ApiResponse<T> =
  | { status: "success", content: T }
  | { status: "error", cause: string }

// export function apiGet<T>(path: string): Promise<T> {
//   return apiGetWithErrorHandler(path, _ => { });
// }

export function apiGet<T>(path: string): Promise<T> {
  return Axios.get<ApiResponse<T>>(`http://localhost:8080/api/${path}`)
    .then(it => {
      const data = it.data;
      switch (data.status) {
        case "error":
          return Promise.reject(data.cause);
        case "success":
          return Promise.resolve(data.content);
      }
    })
} 