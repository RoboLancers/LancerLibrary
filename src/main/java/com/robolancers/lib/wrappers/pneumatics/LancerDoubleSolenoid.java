package com.robolancers.lib.wrappers.pneumatics;

import edu.wpi.first.wpilibj.DoubleSolenoid;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class LancerDoubleSolenoid extends DoubleSolenoid {
    private ArrayList<LancerDoubleSolenoid> followers;

    public LancerDoubleSolenoid(int forwardChannel, int reverseChannel) {
        super(forwardChannel, reverseChannel);

        followers = new ArrayList<>();
    }

    public LancerDoubleSolenoid(int moduleNumber, int forwardChannel, int reverseChannel) {
        super(moduleNumber, forwardChannel, reverseChannel);

        followers = new ArrayList<>();
    }

    public void addFollowers(LancerDoubleSolenoid lancerDoubleSolenoid){
        followers.add(lancerDoubleSolenoid);
    }

    @Override
    public void set(Value value){
        super.set(value);

        for (LancerDoubleSolenoid follower : followers) {
            follower.set(value);
        }
    }
}
