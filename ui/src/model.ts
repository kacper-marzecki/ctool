export interface StoredCommand {
    name: string,
    commandString: string,
    args: string,
    dir: string,
    uses: number
}



export type ApiResponse<T> =
    | { status: "success", content: T }
    | { status: "error", cause: string }
