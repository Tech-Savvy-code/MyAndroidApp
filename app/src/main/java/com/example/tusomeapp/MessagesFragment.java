package com.example.tusomeapp;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private ImageView partnerStatus, connectionStatusIcon;
    private TextView toolbarTitle, connectionStatusText, connectionTime, typingIndicatorText;
    private Button retryButton;
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

    private void initializeViews(View view) {
        // Make sure these IDs match exactly with your fragment_messages.xml
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        attachButton = view.findViewById(R.id.attachButton);

        // These should be LinearLayout
        emptyState = view.findViewById(R.id.emptyState);
        messageInputLayout = view.findViewById(R.id.messageInputLayout);
        typingIndicatorLayout = view.findViewById(R.id.typingIndicatorLayout);
        connectionBanner = view.findViewById(R.id.connectionBanner);

        // These should be ImageView
        partnerStatus = view.findViewById(R.id.partnerStatus);
        connectionStatusIcon = view.findViewById(R.id.connectionStatusIcon);

        // These should be TextView
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        connectionStatusText = view.findViewById(R.id.connectionStatusText);
        connectionTime = view.findViewById(R.id.connectionTime);
        typingIndicatorText = view.findViewById(R.id.typingIndicatorText);

        // This should be Button
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
        boolean hasActiveSession = true; // Replace with real logic

        if (hasActiveSession) {
            showChatInterface();
            addSampleMessages();
        } else {
            showEmptyState();
        }
    }

    private void showChatInterface() {
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        if (messageInputLayout != null) messageInputLayout.setVisibility(View.VISIBLE);
        if (messagesRecyclerView != null) messagesRecyclerView.setVisibility(View.VISIBLE);
        if (toolbarTitle != null) toolbarTitle.setText("Study Partner");
    }

    private void showEmptyState() {
        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        if (messageInputLayout != null) messageInputLayout.setVisibility(View.GONE);
        if (messagesRecyclerView != null) messagesRecyclerView.setVisibility(View.GONE);
    }

    private void addSampleMessages() {
        messageList.add(new Message("Hello! Ready for our math session?", "Partner", System.currentTimeMillis() - 3600000, false, Message.TYPE_TEXT));
        messageList.add(new Message("Yes, I need help with algebra", "You", System.currentTimeMillis() - 1800000, true, Message.TYPE_TEXT));
        messageList.add(new Message("Great! I'll share some practice problems", "Partner", System.currentTimeMillis() - 1200000, false, Message.TYPE_TEXT));
        if (messageAdapter != null) {
            messageAdapter.notifyDataSetChanged();
        }
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            Message newMessage = new Message(messageText, "You", System.currentTimeMillis(), true, Message.TYPE_TEXT);
            messageList.add(newMessage);
            if (messageAdapter != null) {
                messageAdapter.notifyItemInserted(messageList.size() - 1);
            }
            scrollToBottom();

            if (messageInput != null) {
                messageInput.setText("");
            }
            simulatePartnerResponse(messageText);
            showMessageSentToast();
        }
    }

    private void simulatePartnerTyping() {
        if (typingIndicatorLayout != null) {
            typingIndicatorLayout.setVisibility(View.VISIBLE);
        }
        typingHandler.postDelayed(() -> {
            if (typingIndicatorLayout != null) {
                typingIndicatorLayout.setVisibility(View.GONE);
            }
            isTyping = false;
        }, 2000);
    }

    private void hideTypingIndicator() {
        if (typingIndicatorLayout != null) {
            typingIndicatorLayout.setVisibility(View.GONE);
        }
    }

    private void simulatePartnerResponse(String userMessage) {
        if (typingIndicatorLayout != null) {
            typingIndicatorLayout.setVisibility(View.VISIBLE);
        }

        new Handler().postDelayed(() -> {
            if (typingIndicatorLayout != null) {
                typingIndicatorLayout.setVisibility(View.GONE);
            }

            String response = generateSmartResponse(userMessage);
            Message partnerMessage = new Message(response, "Partner", System.currentTimeMillis(), false, Message.TYPE_TEXT);
            messageList.add(partnerMessage);
            if (messageAdapter != null) {
                messageAdapter.notifyItemInserted(messageList.size() - 1);
            }
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
        connectionHandler.postDelayed(() -> {
            showConnectionBanner("Connecting to partner...", android.R.color.holo_orange_light);
            startConnectionTimer();
        }, 500);

        connectionHandler.postDelayed(() -> {
            updateConnectionStatus("Securing connection...", android.R.color.holo_orange_light);
        }, 2000);

        connectionHandler.postDelayed(() -> {
            updateConnectionStatus("Partner connected • Excellent", android.R.color.holo_green_light);
            if (connectionTime != null) connectionTime.setVisibility(View.VISIBLE);
            if (partnerStatus != null) partnerStatus.setImageResource(android.R.drawable.presence_online);
        }, 3500);

        connectionHandler.postDelayed(() -> {
            if (Math.random() > 0.7) {
                simulateConnectionIssue();
            }
        }, 10000);
    }

    private void showConnectionBanner(String message, int backgroundRes) {
        if (connectionBanner != null) {
            connectionBanner.setVisibility(View.VISIBLE);
            connectionBanner.setBackgroundColor(getResources().getColor(backgroundRes));
        }
        if (connectionStatusIcon != null) connectionStatusIcon.setImageResource(android.R.drawable.ic_dialog_info);
        if (connectionStatusText != null) connectionStatusText.setText(message);
        connectionTimer = 0;
        if (connectionTime != null) connectionTime.setText("0s");
    }

    private void updateConnectionStatus(String message, int backgroundRes) {
        if (connectionBanner != null) {
            connectionBanner.setBackgroundColor(getResources().getColor(backgroundRes));
        }
        if (connectionStatusText != null) connectionStatusText.setText(message);
    }

    private void startConnectionTimer() {
        if (connectionRunnable != null) {
            connectionHandler.removeCallbacks(connectionRunnable);
        }

        connectionRunnable = new Runnable() {
            @Override
            public void run() {
                if (connectionTime != null) {
                    connectionTime.setText(connectionTimer + "s");
                }
                connectionTimer++;
                connectionHandler.postDelayed(this, 1000);
            }
        };
        connectionHandler.postDelayed(connectionRunnable, 1000);
    }

    private void simulateConnectionIssue() {
        updateConnectionStatus("Connection unstable • Trying to reconnect", android.R.color.holo_orange_light);

        connectionHandler.postDelayed(() -> {
            if (Math.random() > 0.3) {
                updateConnectionStatus("Reconnected • Good", android.R.color.holo_green_light);
            } else {
                updateConnectionStatus("Connection lost • Tap to retry", android.R.color.holo_red_light);
            }
        }, 3000);
    }

    private void showAttachmentOptions() {
        Toast.makeText(getContext(), "Attachment options: File, Image, Document", Toast.LENGTH_SHORT).show();
    }

    private void retryConnection() {
        showConnectionBanner("Reconnecting...", android.R.color.holo_orange_light);

        connectionHandler.postDelayed(() -> {
            updateConnectionStatus("Partner connected • Good", android.R.color.holo_green_light);
            Toast.makeText(getContext(), "Connection restored", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void showMessageSentToast() {
        Toast.makeText(getContext(), "✓ Message sent", Toast.LENGTH_SHORT).show();
    }

    private void scrollToBottom() {
        if (messagesRecyclerView != null && messageList.size() > 0) {
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);
        }
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

    // ============================
    // Message Model Class
    // ============================
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

        public String getText() { return text; }
        public String getSender() { return sender; }
        public long getTimestamp() { return timestamp; }
        public boolean isSent() { return isSent; }
        public int getType() { return type; }
    }

    // ============================
    // MessageAdapter Class
    // ============================
    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        private final List<Message> messages;

        public MessageAdapter(List<Message> messages) {
            this.messages = messages;
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
            String time = DateFormat.format("hh:mm a", message.getTimestamp()).toString();

            if (message.isSent()) {
                holder.sentMessageLayout.setVisibility(View.VISIBLE);
                holder.receivedMessageLayout.setVisibility(View.GONE);
                holder.sentMessageText.setText(message.getText());
                holder.sentMessageTime.setText(time);
            } else {
                holder.receivedMessageLayout.setVisibility(View.VISIBLE);
                holder.sentMessageLayout.setVisibility(View.GONE);
                holder.receivedMessageText.setText(message.getText());
                holder.receivedMessageTime.setText(time);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {
            LinearLayout receivedMessageLayout, sentMessageLayout;
            TextView receivedMessageText, sentMessageText, receivedMessageTime, sentMessageTime;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                receivedMessageLayout = itemView.findViewById(R.id.receivedMessageLayout);
                sentMessageLayout = itemView.findViewById(R.id.sentMessageLayout);
                receivedMessageText = itemView.findViewById(R.id.receivedMessageText);
                sentMessageText = itemView.findViewById(R.id.sentMessageText);
                receivedMessageTime = itemView.findViewById(R.id.receivedMessageTime);
                sentMessageTime = itemView.findViewById(R.id.sentMessageTime);
            }
        }
    }
}