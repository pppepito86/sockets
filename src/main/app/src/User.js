import React, { useCallback, useState } from 'react';
import { FullScreen, useFullScreenHandle } from "react-full-screen";
import useWindowFocus from 'use-window-focus';

import { v4 as uuidv4 } from 'uuid';

import {
  StompSessionProvider,
  useStompClient,
  useSubscription,
} from "react-stomp-hooks";
import { useParams } from 'react-router';

const User = () => {
  let { name } = useParams();
  
  return (
    //Initialize Stomp connection, will use SockJS for http(s) and WebSocket for ws(s)
    //The Connection can be used by all child components via the hooks or hocs.
    <StompSessionProvider
      // url={"https://stream.elite12.de/api/sock"}
      url={"http://localhost:8091/websocket-example?token=aaa"}
      connectHeaders={{login: name, passcode: 'password'}}
      //All options supported by @stomp/stompjs can be used here
    >
      <SubscribingComponent />
    </StompSessionProvider>
  );
};

function SubscribingComponent() {
  let { name } = useParams();

  const screen = useFullScreenHandle();
  const windowFocused = useWindowFocus();
  const stompClient = useStompClient();

  const [question, setQuestion] = useState({});
  const [answer, setAnswer] = useState("");

  const reportChange = useCallback((state, handle) => {
    // console.log('Screen went to', state, handle);
  }, [screen]);

  useSubscription("/topic/questions", (message) => setQuestion(JSON.parse(message.body)));

  const sendAnswer = () => {
    if(stompClient) {
      const receiptId = uuidv4();

      stompClient.watchForReceipt(receiptId, (frame) => {
        setQuestion({});
        setAnswer("");
      });

      stompClient.publish({
        destination: "/app/answer",
        body: answer,
        headers: {receipt: receiptId}
      });
    } else {
      //Handle error
    }
  } 

  return (
    <div>
      <h1>Hello, {name}</h1>
      {/* {JSON.stringify(JSON.parse(question.answers))} */}
      {question.question && !question.finished &&
      <div>
        Question: {question.question}
        {question.answers[name] && <div>
          ANSWERED
        </div>}
        {!question.answers[name] && <div>
          <br />
          Answer: <input type="text" value={answer} onChange={e => setAnswer(e.target.value)} />
          <button onClick={sendAnswer}>
            Answer
          </button>
        </div>}
      </div>}
      {question.question && question.finished &&
        <div>
          {Object.entries(question.answers).map((u) => <h3 key={u[0]}>{u[1]}</h3>)}
        </div>
      }

      {/* <br /><br /><br /><br /><br />
      <br /><br /><br /><br /><br />
      <br /><br /><br /><br /><br />
      <button onClick={screen.enter}>
        Enter fullscreen
      </button>
      <FullScreen handle={screen} onChange={reportChange}>
        {screen.active && windowFocused && <div className="full-screenable-node" style={{background: "red"}}>
        Any fullscreen content here
        </div>}
      </FullScreen> */}
  </div>
  );
}

export default User;
