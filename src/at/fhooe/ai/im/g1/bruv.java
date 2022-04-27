package at.fhooe.ai.im.g1;

import robocode.*;
import robocode.util.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class bruv extends AdvancedRobot {

    static LinkedHashMap<String, Double> enemyHashMapAngles;
    static LinkedHashMap<String, Double> enemyHashMapDistances;
    static LinkedHashMap<String, Double> enemyHashMapEnergy;

    static double scanDir;
    static Object sought;
    static Object target;
    private double moveDirection = 1D;

    @Override
    public void run() {

        init();

        while (true) {

            scanForRobots();

            if (enemyHashMapAngles.size() == getOthers()) {
                setTarget();
                fireAtTarget();
            }
        }
    }

    // Called when any robot dies
    public void onRobotDeath(RobotDeathEvent e) {
        String name = e.getName();

        // Forget robot on death
        enemyHashMapAngles.remove(name);
        enemyHashMapDistances.remove(name);
        enemyHashMapEnergy.remove(name);

        sought = null;

        if (target == name) {
            target = null;
        }
    }

    // Called when radar detects a robot
    public void onScannedRobot(ScannedRobotEvent e) {
        String name = e.getName();

        enemyHashMapAngles.put(name, getHeadingRadians() + e.getBearingRadians());    // Store name and angle to scanned robot
        enemyHashMapDistances.put(name, e.getDistance());    // Store name and distance to scanned robot
        enemyHashMapEnergy.put(name, e.getEnergy());    // Store name and distance to scanned robot

        // Change direction of radar
        if ((name == sought || sought == null) && enemyHashMapAngles.size() == getOthers()) {
            scanDir = Utils.normalRelativeAngle(enemyHashMapAngles.values().iterator().next() - getRadarHeadingRadians());
            sought = enemyHashMapAngles.keySet().iterator().next();
        }

        // Movement
        setTurnRight(e.getBearing() + 90D);
        setAhead(100 * moveDirection);
        if (randomChance(5)) {
            switchMoveDirection();
        }
    }

    // Initializes class members
    public void init() {
        scanDir = 1;
        enemyHashMapAngles = new LinkedHashMap<String, Double>(5, 2, true);
        enemyHashMapDistances = new LinkedHashMap<String, Double>(5, 2, true);
        enemyHashMapEnergy = new LinkedHashMap<String, Double>(5, 2, true);

        setAdjustGunForRobotTurn(true);    // Independent rotation of gun from body
        setAdjustRadarForGunTurn(true); // Independent rotation of radar from gun
    }

    // Scans for enemy robots
    public void scanForRobots() {
        setTurnRadarRightRadians(scanDir * Double.POSITIVE_INFINITY); // Keep rotating radar
        scan();
    }

    // Gets the closest enemy robot
    public void setTarget() {
        if (enemyHashMapAngles.size() == getOthers()) {

            Double minDistance = Double.MAX_VALUE;
            Object closestTarget = null;

            for (Map.Entry<String, Double> entry : enemyHashMapDistances.entrySet()) {
                Object name = entry.getKey();
                Double distance = entry.getValue();

                if (distance < minDistance) {
                    minDistance = distance;
                    closestTarget = name;
                }
            }

            target = closestTarget;
        }
    }

    // Rotates gun to target and start firing
    public void fireAtTarget() {
        if (target != null) {
            Double absoluteBearing = enemyHashMapAngles.get(target);

            setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));

            if (getGunHeat() == 0) {
                double energy = enemyHashMapEnergy.get(target);
                double dist = enemyHashMapDistances.get(target);
                double selfEnergy = getEnergy();
                double firePower = 3;

                if (dist > 200) {
                    firePower -= 2;
                } else if (dist > 100) {
                    firePower -= 1;
                }

                if (selfEnergy < 25) {
                    firePower -= 0.5;
                }

                firePower = Clamp(firePower, 0.2, energy);
                fire(firePower);
            }
        }
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        switchMoveDirection();
    }


    private boolean randomChance(double percent) {
        return Math.random() * 100D < percent;
    }


    private void switchMoveDirection() {
        moveDirection *= -1.0;
    }

    private double Clamp(double val, double min, double max) {
        if (val > max) {
            val = max;
        }
        if (val < min) {
            val = min;
        }

        return val;
    }
}
