import React, { useState } from 'react';

import {
  StompSessionProvider,
  useStompClient,
  useSubscription,
} from "react-stomp-hooks";

const Admin = () => {
  return (
    <StompSessionProvider
      url={"http://localhost:8091/websocket-example"}
      connectHeaders={{login: 'admin', passcode: 'admin123'}}
    >
      <SubscribingComponent />
    </StompSessionProvider>
  );
};

function SubscribingComponent() {
  const [users, setUsers] = useState([]);
  const [answers, setAnswers] = useState([]);

  const stompClient = useStompClient();
  useSubscription("/topic/users", (message) => setUsers(JSON.parse(message.body)));
  useSubscription("/user/queue/answers", (message) => setAnswers(JSON.parse(message.body)));

  const sendQuestion = (type, text) => {
    if(stompClient) {
      stompClient.publish({
        destination: `/app/question/${type}`,
        body: text
      });
    } else {
      //Handle error
    }
  }

  return (
    <div>
      <h1>Active Users:</h1>
      {users.filter(u => u !== 'admin').map((u) => <h3 key={u}>{u} - {answers[u]}</h3>)}

      <button onClick={() => sendQuestion('start', 'Трябва да слушаш, за да го разбереш!')}>
        Send question
      </button>
      <button onClick={() => sendQuestion('stop', 'n/a')}>
        Stop question
      </button>
      <button onClick={() => sendQuestion('clear', 'n/a')}>
        Clear question
      </button>
  </div>
  );
}

export default Admin;