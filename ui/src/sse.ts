import {notifyInfo} from "./utils";
import {CommandExecutionMessage} from "./model";




export function createSseConnection(onMessage: (m: CommandExecutionMessage) => void):EventSource {
    const source = new EventSource("http://localhost:8080/api/sse")
    source.onopen = () => notifyInfo("connected");
    source.onmessage = (messageEvent: MessageEvent) => {
        onMessage(JSON.parse(messageEvent.data))
        // notifyInfo(`message received: ${messageEvent.data}`);
    }
    return source;
}