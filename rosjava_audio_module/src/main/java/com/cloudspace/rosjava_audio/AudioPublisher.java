/*
 * Copyright (C) 2014 noda.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cloudspace.rosjava_audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import org.jboss.netty.buffer.ChannelBuffers;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;

import java.nio.ByteOrder;


public class AudioPublisher extends AbstractNodeMain {
	private static final String LOG_TAG = "ROS AUDIO";
	String topicName;

	public AudioPublisher(String topicName) {
		this.topicName = topicName;
	}

	AudioRecord audioRecord;
	public static final int SAMPLE_RATE = 8000;

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("rosjava_audio/mic");
	}

	@Override
	public void onShutdown(Node node){
		if ( audioRecord != null ){
			audioRecord.stop();
		}
	}
	
	@Override
	public void onStart(final ConnectedNode connectedNode) {

		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		final int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);


		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
				AudioFormat.CHANNEL_OUT_STEREO,
				MediaRecorder.AudioEncoder.AMR_NB, bufferSize);


		audioRecord.startRecording();

		final Publisher<audio_common_msgs.AudioData> publisher = connectedNode
				.newPublisher(topicName, audio_common_msgs.AudioData._TYPE);
		
		connectedNode.executeCancellableLoop(new CancellableLoop() {
			final byte[] buffer = new byte[bufferSize];

			@Override
			protected void setup() {
			}
			@Override
			protected void loop() throws InterruptedException {
				try {
					audioRecord.read(buffer, 0, bufferSize);
				} catch (Throwable t) {
					Log.e("Error", "Read write failed");
					t.printStackTrace();
				}
				audio_common_msgs.AudioData data = publisher.newMessage();
				data.setData(ChannelBuffers.copiedBuffer(ByteOrder.LITTLE_ENDIAN, buffer, 0, buffer.length));
				publisher.publish(data);
			}
		});
	}
}
