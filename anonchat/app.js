(() => {
    const status = document.getElementById('status');
    const messages = document.getElementById('messages');
    const form = document.getElementById('message-form');
    const input = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    let stompClient;
    let reconnectTimer;

    function setConnected(connected, text) {
        status.textContent = text;
        sendButton.disabled = !connected;
    }

    function renderMessage(message) {
        const row = document.createElement('p');
        row.className = 'message';

        const sender = document.createElement('span');
        sender.className = 'sender';
        sender.textContent = message.sender || 'Гость';

        const time = document.createElement('time');
        time.className = 'time';
        const date = new Date(message.sentAt);
        time.textContent = Number.isNaN(date.getTime()) ? '' : date.toLocaleTimeString();

        const content = document.createElement('div');
        content.textContent = message.content || '';
        row.append(sender, time, content);
        messages.appendChild(row);
        messages.scrollTop = messages.scrollHeight;
    }

    function connect() {
        if (!window.SockJS || !window.Stomp) {
            setConnected(false, 'Не загрузилась библиотека WebSocket');
            return;
        }

        setConnected(false, 'Подключаемся…');
        const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
        const socket = new SockJS(`${protocol}//${window.location.hostname}:8080/anonchat/ws`);
        stompClient = Stomp.over(socket);
        stompClient.debug = null;
        stompClient.connect({}, () => {
            setConnected(true, 'В сети');
            stompClient.subscribe('/topic/anonchat/messages', (frame) => {
                try {
                    renderMessage(JSON.parse(frame.body));
                } catch (error) {
                    console.error('Не удалось прочитать сообщение чата', error);
                }
            });
        }, () => {
            setConnected(false, 'Нет соединения, переподключаемся…');
            clearTimeout(reconnectTimer);
            reconnectTimer = setTimeout(connect, 2500);
        });
    }

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const content = input.value.trim();
        if (!content || !stompClient || !stompClient.connected) return;

        stompClient.send('/app/anonchat/send', {}, JSON.stringify({ content }));
        input.value = '';
        input.focus();
    });

    connect();
})();
