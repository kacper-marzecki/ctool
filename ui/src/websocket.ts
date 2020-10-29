export function connectWs(
  messageHandler: (message: string) => void,
): WebSocket {
  var ws = new WebSocket("ws://localhost:8080");
  ws.onopen = function () {
    // subscribe to some channels
    ws.send(JSON.stringify({
      //.... some message the I must send when I connect ....
    }));
  };

  ws.onmessage = function (e) {
    messageHandler(e.data);
  };

  ws.onclose = function (e) {
    console.log(
      "Socket is closed. Reconnect will be attempted in 1 second.",
      e.reason,
    );
    setTimeout(function () {
      connect();
    }, 1000);
  };

  ws.onerror = function (err) {
    console.error(
      "Socket encountered error: ",
      JSON.stringify(err),
      "Closing socket",
    );
    ws.close();
  };
  return ws;
}
