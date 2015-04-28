package com.cloudspace.rosjava_audio;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Subscriber;

import audio_common_msgs.AudioData;

public class AudioSubscriber extends AbstractNodeMain {
    AudioTrack audioTrack;
    public static final int SAMPLE_RATE = 8000;
    public String topicName;

    public AudioSubscriber(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(topicName + "/audio_sub");
    }

    @Override
    public void onShutdown(Node node) {
        audioTrack.stop();
    }

    public void pause() {
        if (audioTrack != null) {
            audioTrack.pause();
        }
    }

    public void play() {
        if (audioTrack != null) {
            audioTrack.play();
        }
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        final int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.ROUTE_HEADSET, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO,
                MediaRecorder.AudioEncoder.AMR_NB, bufferSize,
                AudioTrack.MODE_STREAM);

        audioTrack.setPlaybackRate(SAMPLE_RATE);
        Subscriber<AudioData> subscriber = connectedNode
                .newSubscriber(topicName,
                        AudioData._TYPE);

        subscriber
                .addMessageListener(new MessageListener<AudioData>() {
                    @Override
                    public void onNewMessage(AudioData message) {
                        byte[] buffer = message.getData().array();
                        audioTrack.write(buffer, 0, buffer.length);
                    }
                });
    }
}
