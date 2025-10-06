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
    private TextView connectionStatusText, connectionTime, typingIndicatorText;
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

        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        checkActiveSession();
        simulateConnectionStatus();

        return view;
    }

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
        connectionStatusIcon = view.findViewById(R.id.connectionStatusIcon);
        connectionStatusText = view.findViewById(R.id.connectionStatusText);
        connectionTime = view.findViewById(R.id.connectionTime);
        typingIndicatorText = view.findViewById(R.id.typingIndicatorText);
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

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isTyping && s.length() > 0) {
                    isTyping = true;
                    simulatePartnerTyping();
                }
            }
            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    isTyping = false;
                    hideTypingIndicator();
                }
            }
        });
    }

    private void checkActiveSession() {
        boolean hasActiveSession = true;
        if (hasActiveSession) {
            emptyState.setVisibility(View.GONE);
            messageInputLayout.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
            addSampleMessages();
        } else {
            emptyState.setVisibility(View.VISIBLE);
            messageInputLayout.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.GONE);
        }
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
            Toast.makeText(getContext(), "✓ Message sent", Toast.LENGTH_SHORT).show();
        }
    }

    private void simulatePartnerTyping() {
        typingIndicatorLayout.setVisibility(View.VISIBLE);
        typingHandler.postDelayed(() -> typingIndicatorLayout.setVisibility(View.GONE), 2000);
    }

    private void hideTypingIndicator() {
        typingIndicatorLayout.setVisibility(View.GONE);
    }

    private void simulatePartnerResponse(String userMessage) {
        typingIndicatorLayout.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            typingIndicatorLayout.setVisibility(View.GONE);
            String response = generateSmartResponse(userMessage);
            messageList.add(new Message(response, "Partner", System.currentTimeMillis(), false, Message.TYPE_TEXT));
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            scrollToBottom();
        }, 2000);
    }

    private String generateSmartResponse(String userMessage) {
        String msg = userMessage.toLowerCase();
        if (msg.contains("hello") || msg.contains("hi")) return "Hello! What topic would you like to discuss today?";
        if (msg.contains("math") || msg.contains("algebra")) return "Let's start with equations or functions?";
        if (msg.contains("question") || msg.contains("help")) return "Sure! Please share your problem.";
        if (msg.contains("thank")) return "You're welcome! Glad to help.";
        if (msg.contains("time") || msg.contains("when")) return "How about scheduling our next session tomorrow?";
        return "That's interesting! Let's explore it further.";
    }

    private void simulateConnectionStatus() {
        connectionHandler.postDelayed(() -> showConnectionBanner("Connecting to partner...", android.R.color.holo_orange_light), 500);
        connectionHandler.postDelayed(() -> updateConnectionStatus("Securing connection...", android.R.color.holo_orange_light), 2000);
        connectionHandler.postDelayed(() -> {
            updateConnectionStatus("Partner connected • Excellent", android.R.color.holo_green_light);
            connectionTime.setVisibility(View.VISIBLE);
            partnerStatus.setImageResource(android.R.drawable.presence_online);
        }, 3500);
    }

    private void showConnectionBanner(String message, int bgColorRes) {
        connectionBanner.setVisibility(View.VISIBLE);
        connectionBanner.setBackgroundColor(getResources().getColor(bgColorRes));
        connectionStatusIcon.setImageResource(android.R.drawable.ic_dialog_info);
        connectionStatusText.setText(message);
        connectionTime.setText("0s");
        connectionTimer = 0;
        startConnectionTimer();
    }

    private void updateConnectionStatus(String message, int bgColorRes) {
        connectionBanner.setBackgroundColor(getResources().getColor(bgColorRes));
        connectionStatusText.setText(message);
    }

    private void startConnectionTimer() {
        if (connectionRunnable != null) connectionHandler.removeCallbacks(connectionRunnable);
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

    private void retryConnection() {
        showConnectionBanner("Reconnecting...", android.R.color.holo_orange_light);
        connectionHandler.postDelayed(() -> {
            updateConnectionStatus("Partner connected • Good", android.R.color.holo_green_light);
            Toast.makeText(getContext(), "Connection restored", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void showAttachmentOptions() {
        Toast.makeText(getContext(), "Attachment options: File, Image, Document", Toast.LENGTH_SHORT).show();
    }

    private void scrollToBottom() {
        if (messageList.size() > 0)
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (connectionRunnable != null)
            connectionHandler.removeCallbacks(connectionRunnable);
        typingHandler.removeCallbacksAndMessages(null);
    }

    // =====================
    // Message model
    // =====================
    public static class Message {
        public static final int TYPE_TEXT = 0;
        public static final int TYPE_IMAGE = 1;
        public static final int TYPE_FILE = 2;
        private final String text, sender;
        private final long timestamp;
        private final boolean isSent;
        private final int type;

        public Message(String text, String sender, long timestamp, boolean isSent, int type) {
            this.text = text; this.sender = sender; this.timestamp = timestamp; this.isSent = isSent; this.type = type;
        }
        public String getText() { return text; }
        public String getSender() { return sender; }
        public long getTimestamp() { return timestamp; }
        public boolean isSent() { return isSent; }
        public int getType() { return type; }
    }

    // =====================
    // Adapter
    // =====================
    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        private final List<Message> messages;
        public MessageAdapter(List<Message> messages) { this.messages = messages; }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Message message = messages.get(position);
            String time = DateFormat.format("hh:mm a", message.getTimestamp()).toString();
            if (message.isSent()) {
                holder.sentLayout.setVisibility(View.VISIBLE);
                holder.receivedLayout.setVisibility(View.GONE);
                holder.sentText.setText(message.getText());
                holder.sentTime.setText(time);
            } else {
                holder.receivedLayout.setVisibility(View.VISIBLE);
                holder.sentLayout.setVisibility(View.GONE);
                holder.receivedText.setText(message.getText());
                holder.receivedTime.setText(time);
            }
        }

        @Override
        public int getItemCount() { return messages.size(); }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            LinearLayout receivedLayout, sentLayout;
            TextView receivedText, sentText, receivedTime, sentTime;
            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                receivedLayout = itemView.findViewById(R.id.receivedMessageLayout);
                sentLayout = itemView.findViewById(R.id.sentMessageLayout);
                receivedText = itemView.findViewById(R.id.receivedMessageText);
                sentText = itemView.findViewById(R.id.sentMessageText);
                receivedTime = itemView.findViewById(R.id.receivedMessageTime);
                sentTime = itemView.findViewById(R.id.sentMessageTime);
            }
        }
    }
}
