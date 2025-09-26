package com.example.tusomeapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton, attachButton;
    private LinearLayout emptyState, messageInputLayout, typingIndicatorLayout, connectionBanner;
    private ImageView partnerStatus, typingIndicator, connectionStatusIcon;
    private TextView toolbarTitle, connectionStatusText, connectionTime;
    private ImageButton retryButton;
    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();
    private final Handler typingHandler = new Handler();
    private final Handler connectionHandler = new Handler();
    private boolean isTyping = false;
    private int connectionTimer = 0;
    private Runnable connectionRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Initialize all views
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        checkActiveSession();
        simulateConnectionStatus();

        return view;
    }

    @SuppressLint("WrongViewCast")
    private void initializeViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        attachButton = view.findViewById(R.id.attachButton);
        emptyState = view.findViewById(R.id.emptyState);
        messageInputLayout = view.findViewById(R.id.messageInputLayout);
        typingIndicatorLayout = view.findViewById(R.id.typingIndicatorLayout);
        connectionBanner = view.findViewById(R.id.connectionBanner);
        partnerStatus = view.findViewById(R.id.partnerStatus);
        typingIndicator = view.findViewById(R.id.typingIndicator);
        connectionStatusIcon = view.findViewById(R.id.connectionStatusIcon);
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        connectionStatusText = view.findViewById(R.id.connectionStatusText);
        connectionTime = view.findViewById(R.id.connectionTime);
        retryButton = view.findViewById(R.id.retryButton);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messageList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        attachButton.setOnClickListener(v -> showAttachmentOptions());
        retryButton.setOnClickListener(v -> retryConnection());

        // Typing indicator
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isTyping && s.length() > 0) {
                    isTyping = true;
                    simulatePartnerTyping();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    isTyping = false;
                    hideTypingIndicator();
                }
            }
        });
    }

    private void checkActiveSession() {
        boolean hasActiveSession = true; // Your session check logic

        if (hasActiveSession) {
            showChatInterface();
            addSampleMessages();
        } else {
            showEmptyState();
        }
    }

    private void showChatInterface() {
        emptyState.setVisibility(View.GONE);
        messageInputLayout.setVisibility(View.VISIBLE);
        messagesRecyclerView.setVisibility(View.VISIBLE);
        toolbarTitle.setText("Study Partner");
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        messageInputLayout.setVisibility(View.GONE);
        messagesRecyclerView.setVisibility(View.GONE);
    }

    private void addSampleMessages() {
        messageList.add(new Message("Hello! Ready for our math session?", "Partner", System.currentTimeMillis() - 3600000, false, Message.TYPE_TEXT));
        messageList.add(new Message("Yes, I need help with algebra", "You", System.currentTimeMillis() - 1800000, true, Message.TYPE_TEXT));
        messageList.add(new Message("Great! I'll share some practice problems", "Partner", System.currentTimeMillis() - 1200000, false, Message.TYPE_TEXT));
        messageAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            Message newMessage = new Message(messageText, "You", System.currentTimeMillis(), true, Message.TYPE_TEXT);
            messageList.add(newMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            scrollToBottom();

            messageInput.setText("");
            simulatePartnerResponse(messageText);
            showMessageSentToast();
        }
    }

    private void simulatePartnerTyping() {
        typingIndicator.setVisibility(View.VISIBLE);
        typingHandler.postDelayed(() -> {
            typingIndicator.setVisibility(View.GONE);
            isTyping = false;
        }, 2000);
    }

    private void hideTypingIndicator() {
        typingIndicator.setVisibility(View.GONE);
    }

    private void simulatePartnerResponse(String userMessage) {
        typingIndicatorLayout.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            typingIndicatorLayout.setVisibility(View.GONE);

            String response = generateSmartResponse(userMessage);
            Message partnerMessage = new Message(response, "Partner", System.currentTimeMillis(), false, Message.TYPE_TEXT);
            messageList.add(partnerMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            scrollToBottom();
        }, 2000);
    }

    private String generateSmartResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "Hello! What topic would you like to discuss today?";
        } else if (lowerMessage.contains("math") || lowerMessage.contains("algebra")) {
            return "I can help with that! Should we start with equations or functions?";
        } else if (lowerMessage.contains("question") || lowerMessage.contains("help")) {
            return "I'd be happy to help! Can you share the specific problem?";
        } else if (lowerMessage.contains("thank")) {
            return "You're welcome! Let me know if you need more help.";
        } else if (lowerMessage.contains("time") || lowerMessage.contains("when")) {
            return "We can schedule another session. How about tomorrow?";
        } else {
            return "That's interesting! Let's explore this topic together.";
        }
    }

    private void simulateConnectionStatus() {
        // Simulate realistic connection sequence
        connectionHandler.postDelayed(() -> {
            showConnectionBanner("Connecting to partner...", R.drawable.connection_banner_warning, R.drawable.ic_wifi);
            startConnectionTimer();
        }, 500);

        connectionHandler.postDelayed(() -> {
            updateConnectionStatus("Securing connection...", R.drawable.connection_banner_warning, R.drawable.ic_wifi);
        }, 2000);

        connectionHandler.postDelayed(() -> {
            updateConnectionStatus("Partner connected • Excellent", R.drawable.connection_banner_gradient, R.drawable.ic_signal_wifi_4_bar);
            connectionTime.setVisibility(View.VISIBLE);
            partnerStatus.setImageResource(R.drawable.ic_online);
        }, 3500);

        connectionHandler.postDelayed(() -> {
            // Simulate occasional connection issues
            if (Math.random() > 0.7) { // 30% chance of connection issue
                simulateConnectionIssue();
            }
        }, 10000);
    }

    private void showConnectionBanner(String message, int backgroundRes, int iconRes) {
        connectionBanner.setVisibility(View.VISIBLE);
        connectionBanner.setBackgroundResource(backgroundRes);
        connectionStatusIcon.setImageResource(iconRes);
        connectionStatusText.setText(message);
        connectionTimer = 0;
        connectionTime.setText("0s");
    }

    private void updateConnectionStatus(String message, int backgroundRes, int iconRes) {
        connectionBanner.setBackgroundResource(backgroundRes);
        connectionStatusIcon.setImageResource(iconRes);
        connectionStatusText.setText(message);
    }

    private void startConnectionTimer() {
        if (connectionRunnable != null) {
            connectionHandler.removeCallbacks(connectionRunnable);
        }

        connectionRunnable = new Runnable() {
            @Override
            public void run() {
                connectionTime.setText(connectionTimer + "s");
                connectionTimer++;
                connectionHandler.postDelayed(this, 1000);
            }
        };
        connectionHandler.postDelayed(connectionRunnable, 1000);
    }

    private void simulateConnectionIssue() {
        updateConnectionStatus("Connection unstable • Trying to reconnect", R.drawable.connection_banner_warning, R.drawable.ic_wifi);

        connectionHandler.postDelayed(() -> {
            if (Math.random() > 0.3) { // 70% chance of successful reconnect
                updateConnectionStatus("Reconnected • Good", R.drawable.connection_banner_gradient, R.drawable.ic_signal_wifi_4_bar);
            } else {
                updateConnectionStatus("Connection lost • Tap to retry", R.drawable.connection_banner_error, R.drawable.ic_wifi);
            }
        }, 3000);
    }

    private void showAttachmentOptions() {
        Toast.makeText(getContext(), "Attachment options: File, Image, Document", Toast.LENGTH_SHORT).show();
    }

    private void retryConnection() {
        showConnectionBanner("Reconnecting...", R.drawable.connection_banner_warning, R.drawable.ic_wifi);

        connectionHandler.postDelayed(() -> {
            updateConnectionStatus("Partner connected • Good", R.drawable.connection_banner_gradient, R.drawable.ic_signal_wifi_4_bar);
            Toast.makeText(getContext(), "Connection restored", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void showMessageSentToast() {
        Toast.makeText(getContext(), "✓ Message sent", Toast.LENGTH_SHORT).show();
    }

    private void scrollToBottom() {
        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (connectionHandler != null && connectionRunnable != null) {
            connectionHandler.removeCallbacks(connectionRunnable);
        }
        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }
    }

    // Enhanced Message class
    public static class Message {
        public static final int TYPE_TEXT = 0;
        public static final int TYPE_IMAGE = 1;
        public static final int TYPE_FILE = 2;

        private String text;
        private String sender;
        private long timestamp;
        private boolean isSent;
        private int type;

        public Message(String text, String sender, long timestamp, boolean isSent, int type) {
            this.text = text;
            this.sender = sender;
            this.timestamp = timestamp;
            this.isSent = isSent;
            this.type = type;
        }

        // Getters
        public String getText() { return text; }
        public String getSender() { return sender; }
        public long getTimestamp() { return timestamp; }
        public boolean isSent() { return isSent; }
        public int getType() { return type; }
    }

    // MessageAdapter
    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        private List<Message> messages;

        public MessageAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isSent() ? 1 : 0;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Message message = messages.get(position);

            if (message.isSent()) {
                holder.sentMessageLayout.setVisibility(View.VISIBLE);
                holder.receivedMessageLayout.setVisibility(View.GONE);
                holder.sentMessageText.setText(message.getText());
            } else {
                holder.receivedMessageLayout.setVisibility(View.VISIBLE);
                holder.sentMessageLayout.setVisibility(View.GONE);
                holder.receivedMessageText.setText(message.getText());
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {
            LinearLayout receivedMessageLayout, sentMessageLayout;
            TextView receivedMessageText, sentMessageText;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                receivedMessageLayout = itemView.findViewById(R.id.receivedMessageLayout);
                sentMessageLayout = itemView.findViewById(R.id.sentMessageLayout);
                receivedMessageText = itemView.findViewById(R.id.receivedMessageText);
                sentMessageText = itemView.findViewById(R.id.sentMessageText);
            }
        }
    }
}