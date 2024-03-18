package dev.thomazz.pledge.pinger.frame.data;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class FrameData {
    private final Queue<Frame> expectingFrames = new ConcurrentLinkedQueue<>();
    private final AtomicReference<Frame> currentFrame = new AtomicReference<>();

    public boolean hasFrame() {
        return this.currentFrame.get() != null;
    }

    public void setFrame(Frame frame) {
        this.currentFrame.set(frame);
    }

    public Frame getFrame() {
        return this.currentFrame.get();
    }

    public Optional<Frame> continueFrame() {
        Frame frame = this.currentFrame.getAndSet(null);

        if (frame != null) {
            this.expectingFrames.add(frame);
        }

        return Optional.ofNullable(frame);
    }

    public Optional<Frame> matchStart(int id) {
        return Optional.ofNullable(this.expectingFrames.peek()).filter(frame -> frame.getStartId() == id);
    }

    public Optional<Frame> matchEnd(int id) {
        return Optional.ofNullable(this.expectingFrames.peek()).filter(frame -> frame.getEndId() == id);
    }

    public void popFrame() {
        this.expectingFrames.poll();
    }
}
