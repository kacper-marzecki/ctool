import { FormInstance, RuleObject } from "antd/lib/form";
import { StoreValue } from "antd/lib/form/interface";
import Axios from "axios";
import { promises } from "dns";
import { match, __ } from "ts-pattern";
import { StructuredType } from "typescript";

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

/**
 * AntD string validator 
 */
export const notEmpty = (rule: RuleObject, value: StoreValue, callback: (error?: string) => void) => {

  if ((value as string).trim().length === 0) {
    // TODO internationalize ?
    callback("Cannot be empty")
  }
}

export const formTouchedAndValid = (form: FormInstance<any>) => {
  return !form.isFieldsTouched(false) ||
    form.getFieldsError().filter(({ errors }) => errors.length).length !== 0
}

export function wrapInField<K extends keyof any, T>(fieldName: K): (value: T) => { [P in K]: T } {
  return (value) => {
    let wrapper = {} as { [P in K]: T }
    wrapper[fieldName] = value;
    return wrapper;
  }
}