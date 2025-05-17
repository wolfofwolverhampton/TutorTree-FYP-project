let localVideo = document.getElementById("local-video");
let remoteVideo = document.getElementById("remote-video");

localVideo.style.opacity = 0;
remoteVideo.style.opacity = 0;

localVideo.onplaying = () => { localVideo.style.opacity = 1; };
remoteVideo.onplaying = () => { remoteVideo.style.opacity = 1; };

let currentCameraIndex = 0;
let availableCameras = [];

async function getAvailableCameras() {
    const devices = await navigator.mediaDevices.enumerateDevices();
    availableCameras = devices.filter(device => device.kind === 'videoinput');
    console.log("Available cameras:", availableCameras);
}

let peer;
function init(userId) {
    peer = new Peer(userId, {
        host: '192.168.1.200',
        port: 9000,
        path: '/JavaInternal'

    });

    peer.on('open', () => {
        Android.onPeerConnected();
    });

    peer.on('error', (err) => {
        console.error("PeerJS error:", err);
    });

    peer.on('close', () => {
        console.log("PeerJS connection closed");
    });

    listen();

    getAvailableCameras();
}

let localStream;
function listen() {
    peer.on('call', (call) => {
        const videoConstraints = {
            width: { ideal: 1280, max: 1920 },
            height: { ideal: 720, max: 1080 },
            frameRate: { ideal: 30, max: 60 }
        };

        navigator.mediaDevices.getUserMedia({
            audio: true,
            video: videoConstraints
        }).then((stream) => {
            localVideo.srcObject = stream;
            localStream = stream;

            call.answer(stream);
            call.on('stream', (remoteStream) => {
                remoteVideo.srcObject = remoteStream;

                remoteVideo.className = "primary-video";
                localVideo.className = "secondary-video";
            });
        }).catch((error) => {
            console.error("Error accessing media devices:", error);
        });
    });
}

function startCall(otherUserId) {
    getAvailableCameras();
    const videoConstraints = {
        width: { ideal: 1280, max: 1920 },
        height: { ideal: 720, max: 1080 },
        frameRate: { ideal: 30, max: 60 }
    };

    navigator.mediaDevices.getUserMedia({
        audio: true,
        video: videoConstraints
    }).then((stream) => {
        localVideo.srcObject = stream;
        localStream = stream;

        const call = peer.call(otherUserId, stream);
        call.on('stream', (remoteStream) => {
            remoteVideo.srcObject = remoteStream;

            remoteVideo.className = "primary-video";
            localVideo.className = "secondary-video";
        });
    }).catch((error) => {
        console.error("Error accessing media devices:", error);
    });
}

function toggleVideo(b) {
    if (b == "true") {
        localStream.getVideoTracks().forEach(track => track.enabled = true);
    } else {
        localStream.getVideoTracks().forEach(track => track.enabled = false);
    }
}

function toggleAudio(b) {
    if (b == "true") {
        localStream.getAudioTracks().forEach(track => track.enabled = true);
    } else {
        localStream.getAudioTracks().forEach(track => track.enabled = false);
    }
}

window.flipCamera = async () => {
    if (availableCameras.length < 2) {
        console.warn("Only one camera available. Cannot flip.");
        return;
    }

    currentCameraIndex = (currentCameraIndex + 1) % availableCameras.length;

    const selectedCamera = availableCameras[currentCameraIndex];
    const videoConstraints = {
        deviceId: { exact: selectedCamera.deviceId },
        width: { ideal: 1280, max: 1920 },
        height: { ideal: 720, max: 1080 },
        frameRate: { ideal: 30, max: 60 }
    };

    try {
        localStream.getVideoTracks().forEach(track => track.stop());

        const newStream = await navigator.mediaDevices.getUserMedia({
            audio: true,
            video: videoConstraints
        });

        localStream = newStream;
        localVideo.srcObject = newStream;

        if (peer && peer._connections) {
            Object.values(peer._connections).forEach(connection => {
                connection.forEach(call => {
                    call.peerConnection.getSenders().forEach(sender => {
                        if (sender.track && sender.track.kind === 'video') {
                            sender.replaceTrack(newStream.getVideoTracks()[0]);
                        }
                    });
                });
            });
        }
    } catch (error) {
        console.error("Error switching camera:", error);
    }
};
