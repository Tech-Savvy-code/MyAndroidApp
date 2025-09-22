package com.example.tusomeapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesFragment extends Fragment {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private LinearLayout emptyState, messageInputLayout;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Initialize views
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        emptyState = view.findViewById(R.id.emptyState);
        messageInputLayout = view.findViewById(R.id.messageInputLayout);

        // Setup RecyclerView
        setupRecyclerView();

        // Set up button listeners
        sendButton.setOnClickListener(v -> sendMessage());

        // Check if there are active sessions to enable messaging
        checkActiveSession();

        return view;
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messageList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void checkActiveSession() {
        // For demo purposes, let's assume we have an active session
        boolean hasActiveSession = true; // Change this based on your logic

        if (hasActiveSession) {
            emptyState.setVisibility(View.GONE);
            messageInputLayout.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.VISIBLE);

            // Add sample messages for demo
            addSampleMessages();
        } else {
            emptyState.setVisibility(View.VISIBLE);
            messageInputLayout.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void addSampleMessages() {
        // Sample messages for demonstration
        messageList.add(new Message("Hello! Ready for our session?", "Partner", System.currentTimeMillis() - 3600000, false));
        messageList.add(new Message("Yes, I'm excited to learn!", "You", System.currentTimeMillis() - 1800000, true));
        messageList.add(new Message("Great! Let me share the materials first", "Partner", System.currentTimeMillis() - 1200000, false));
        messageAdapter.notifyDataSetChanged();
        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Create new message
            Message newMessage = new Message(messageText, "You", System.currentTimeMillis(), true);
            messageList.add(newMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);

            // Clear input
            messageInput.setText("");

            // Simulate partner response after delay
            simulatePartnerResponse(messageText);

            Toast.makeText(getContext(), "Message sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    private void simulatePartnerResponse(String userMessage) {
        // Simulate AI response after 1-2 seconds
        new android.os.Handler().postDelayed(() -> {
            String response;
            if (userMessage.toLowerCase().contains("hello") || userMessage.toLowerCase().contains("hi")) {
                response = "Hello! How can I help you with your studies today?";
            } else if (userMessage.toLowerCase().contains("question")) {
                response = "I'd be happy to help with your question!";
            } else if (userMessage.toLowerCase().contains("thank")) {
                response = "You're welcome! I'm here to help.";
            } else {
                response = "Thanks for your message! I'm here to help you learn.";
            }

            Message partnerMessage = new Message(response, "Partner", System.currentTimeMillis(), false);
            messageList.add(partnerMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);
        }, 1000 + (long)(Math.random() * 1000));
    }

    // Message class
    public static class Message {
        private String text;
        private String sender;
        private long timestamp;
        private boolean isSent;

        public Message(String text, String sender, long timestamp, boolean isSent) {
            this.text = text;
            this.sender = sender;
            this.timestamp = timestamp;
            this.isSent = isSent;
        }

        public String getText() { return text; }
        public String getSender() { return sender; }
        public long getTimestamp() { return timestamp; }
        public boolean isSent() { return isSent; }
    }

    // Complete MessageAdapter class
    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private List<Message> messages;

        public MessageAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            // Return 0 for received messages, 1 for sent messages
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
                // Show sent message layout, hide received
                holder.sentMessageLayout.setVisibility(View.VISIBLE);
                holder.receivedMessageLayout.setVisibility(View.GONE);
                holder.sentMessageText.setText(message.getText());
            } else {
                // Show received message layout, hide sent
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