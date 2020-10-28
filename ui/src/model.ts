export interface StoredCommand {
    name: string,
    commandString: string,
    args: string[],
    dir: string,
    uses: number
}

export interface CommandExecution {
    commandString: string,
    args: string[],
    dir: string,
    time: number
}



export type ApiResponse<T> =
    | { status: "success", content: T }
    | { status: "error", cause: string }
