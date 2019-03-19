package game;

public class GameThread extends Thread {
    private static final String DEBUG_TAG = "GAME_THREAD: ";
    private static final boolean LOG_TPS = false;

    public static final int MAX_TPS = 30;
    private double averageTPS;
    private boolean running;

    private GameCore gameCore;


    public GameThread(GameCore gameCore) {
        super();
        running = false;
        this.gameCore = gameCore;
    }

    @Override
    public void run() {

        long startTime;
        long timeMillis = 1000/ MAX_TPS;
        long waitTime;
        int tickCount = 0;
        long totalTime = 0;
        long targetTime = 1000/ MAX_TPS;

        running = true;
        while(true){
            if(running) {
                startTime = System.nanoTime();

                gameCore.tick();

                timeMillis = (System.nanoTime() - startTime) / 1000000;
                waitTime = targetTime - timeMillis;
                try {
                    if (waitTime > 0) {
                        this.sleep(waitTime);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                totalTime += System.nanoTime() - startTime;
                tickCount++;
                if (tickCount == MAX_TPS) {
                    averageTPS = ((float)tickCount/totalTime)*1000000000;
                    tickCount = 0;
                    totalTime = 0;
                    if(LOG_TPS) System.out.println(DEBUG_TAG+ "TPS: " + averageTPS);
                }
            } else {
                //wait here until game is resumed

            }
        }

    }

}
