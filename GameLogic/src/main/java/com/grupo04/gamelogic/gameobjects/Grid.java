package com.grupo04.gamelogic.gameobjects;

import static com.grupo04.engine.utilities.JSONConverter.convertJSONArrayToMatrix;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.GameObject;
import com.grupo04.engine.utilities.Pair;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.engine.interfaces.IAudio;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.gamelogic.BubbleColors;
import com.grupo04.gamelogic.gameobjects.buttons.ImageToggleButton;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Grid extends GameObject {
    // Linea del final del nivel
    private final int lineThickness;
    private final Color lineColor;

    // Matriz de burbujas con los colores de las mismas
    private int[][] bubbles;
    // Numero de filas y columnas
    private final int rows;
    private final int cols;

    // Numero total de burbujas
    // Cuando se ha llegado a 0, se gana la partida
    private int totalBubbles;
    // Indica cuantas bolas hay de cada color en el mapa
    // Se usa para saber de que color puede ser la bola que se lanza
    private int[] colorCount;
    // 5 puntos por cada burbuja en un grupo de 3 y 10 puntos por cada burbuja en un grupo de 10 o mas

    // Radio de las burbujas
    private final int r;
    // Radio de los hexagonos de la cuadricula
    private final float hexagonRadius;
    // Offsets para las paredes y la cabecera
    private final int offsetX;
    private final int offsetY;
    // Offsets para que no haya mucho espacio entre las bolas en y
    private final int bubbleOffset;

    // Linea del final del nivel
    private final Vector lineInit;
    private final Vector lineEnd;

    // Posibles posiciones adyacentes a cada bola
    private final List<Pair<Integer, Integer>> oddAdjacentCells;
    private final List<Pair<Integer, Integer>> evenAdjacentCells;

    // Bolas necesarias que esten juntas para que se produzca la explosion
    private final int bubblesToExplode;
    // Puntuacion que se otorga si el grupo de bolas que se forma es del tamano bubblesToExplode + 1
    private final int greatScore;
    // Puntuacion que se otorga si el grupo de bolas que se forma es del tamano bubblesToExplode
    private final int smallScore;
    // Puntuacion actual
    private int score;

    private final float fallingSpeed;
    private final List<Pair<Vector, Integer>> fallingBubbles;
    private boolean end;
    private boolean won;

    static class AnimCollidedBubbles {
        public Vector pos;
        public int color;
        public float radius;
    }

    private final List<AnimCollidedBubbles> collidedBubbles;
    private final float shrinkSpeed;

    private boolean[][] visited;

    private IEngine engine;
    private IAudio audio;
    private ISound attachSound;
    private ISound explosionSound;

    private BubbleColors bubbleColors;

    private WeakReference<Text> scoreText;
    private WeakReference<ImageToggleButton> showGridButton;

    // DEBUG DE LAS CELDAS
    private int currI;
    private int currJ;

    public Grid(JSONObject progressJson, int width, int wallThickness, int headerOffset, int r, int bubbleOffset, int rows, int cols, int initRows,
                int bubblesToExplode, int greatScore, int smallScore, BubbleColors bubbleColors, float fallingSpeed, float shrinkSpeed) {
        super();

        this.lineThickness = 1;
        this.lineColor = new Color(0, 0, 0, 255);

        this.cols = cols;
        this.rows = rows;

        this.r = r;
        this.hexagonRadius = (float) Math.ceil((this.r / (Math.sqrt(3) / 2.0f)));
        this.offsetX = wallThickness;
        this.offsetY = wallThickness + headerOffset;
        this.bubbleOffset = bubbleOffset;

        // Se generan initRows filas iniciales
        this.totalBubbles = 0;

        this.bubbleColors = bubbleColors;
        this.colorCount = new int[this.bubbleColors.getTotalColors()];
        this.bubbleColors.reset();

        this.bubbles = new int[this.rows][this.cols];
        for (int[] row : this.bubbles) {
            Arrays.fill(row, -1);
        }

        if (!tryLoadingProgress(progressJson)) {
            for (int i = 0; i < initRows; i++) {
                // En las filas impares hay una bola menos
                int bPerRow = (i % 2 == 0) ? this.cols : (this.cols - 1);

                // Se generan las burbujas de la fila
                for (int j = 0; j < bPerRow; ++j) {
                    int color = this.bubbleColors.generateRandomColor();
                    this.bubbles[i][j] = color;
                    this.bubbleColors.addColor(color);
                    if (color >= 0 && color < this.bubbleColors.getTotalColors()) {
                        this.colorCount[color]++;
                        this.totalBubbles++;
                    }
                }
            }

            this.score = 0;
        }

        int lineY = (this.r * 2 * (this.rows - 1)) - this.bubbleOffset * (this.rows - 2);
        this.lineInit = new Vector(this.offsetX, this.offsetY + lineY);
        this.lineEnd = new Vector(width - this.offsetX, this.offsetY + lineY);

        // Creamos la lista de celdas adyacentes a cada posicion
        this.oddAdjacentCells = new ArrayList<>();
        this.oddAdjacentCells.add(new Pair<>(-1, 0));    // Arriba izquierda
        this.oddAdjacentCells.add(new Pair<>(-1, 1));    // Arriba derecha
        this.oddAdjacentCells.add(new Pair<>(0, -1));    // Izquierda
        this.oddAdjacentCells.add(new Pair<>(0, 1));     // Derecha
        this.oddAdjacentCells.add(new Pair<>(1, 0));     // Abajo izquierda
        this.oddAdjacentCells.add(new Pair<>(1, 1));     // Abajo derecha

        this.evenAdjacentCells = new ArrayList<>();
        this.evenAdjacentCells.add(new Pair<>(-1, -1));     // Arriba izquierda
        this.evenAdjacentCells.add(new Pair<>(-1, 0));      // Arriba derecha
        this.evenAdjacentCells.add(new Pair<>(0, -1));      // Izquierda
        this.evenAdjacentCells.add(new Pair<>(0, 1));       // Derecha
        this.evenAdjacentCells.add(new Pair<>(1, -1));      // Abajo izquierda
        this.evenAdjacentCells.add(new Pair<>(1, 0));       // Abajo derecha

        this.bubblesToExplode = bubblesToExplode;
        this.greatScore = greatScore;
        this.smallScore = smallScore;

        this.fallingSpeed = fallingSpeed;
        this.fallingBubbles = new ArrayList<>();
        this.won = false;
        this.end = false;

        this.collidedBubbles = new ArrayList<>();
        this.shrinkSpeed = shrinkSpeed;

        this.visited = new boolean[this.rows][this.cols];

        this.engine = null;
        this.audio = null;
        this.attachSound = null;
        this.explosionSound = null;

        this.scoreText = null;
        this.showGridButton = null;

        // DEBUG
        this.currI = -1;
        this.currJ = -1;
    }

    public Grid(JSONObject progressJson, int width, int wallThickness, int headerOffset, int r, int bubbleOffset, int rows, int cols, int initRows,
                int bubblesToExplode, int greatScore, int smallScore, BubbleColors bubbleColors) {
        this(progressJson, width, wallThickness, headerOffset, r, bubbleOffset, rows, cols, initRows,
                bubblesToExplode, greatScore, smallScore, bubbleColors, 350f, 60f);
    }


    @Override
    public void init() {
        this.engine = this.scene.getEngine();
        this.audio = this.engine.getAudio();
        this.attachSound = this.audio.newSound("ballAttach.wav");
        this.explosionSound = this.audio.newSound("ballExplosion.wav");
        this.scoreText = new WeakReference<>((Text) this.scene.getHandler("scoreText"));
        updateScoreText();
        this.showGridButton = new WeakReference<>((ImageToggleButton) this.scene.getHandler("showGridButton"));
    }

    @Override
    public void render(IGraphics graphics) {
        super.render(graphics);

        ImageToggleButton showGridButtonRef = this.showGridButton.get();

        // Recorre la matriz y pinta las bolas si el color en la posicion i,j de la matriz es >= 0
        if (this.bubbles != null) {
            for (int i = 0; i < this.rows; i++) {
                int bPerRow = (i % 2 == 0) ? this.cols : (this.cols - 1);
                for (int j = 0; j < bPerRow; ++j) {
                    Vector pos = gridToWorldPosition(i, j);
                    this.bubbleColors.drawBall(graphics, scene.getGameManager(), this.bubbles[i][j], pos, this.r);

                    if (showGridButtonRef != null && showGridButtonRef.isCheck()) {
                        pos.x += 0.5f;
                        graphics.setColor(this.lineColor);
                        graphics.drawHexagon(pos, this.hexagonRadius, 90, this.lineThickness);
                    }
                }
            }
        }

        // DEBUG
        //debugCollisions(graphics);

        // Pinta la linea del limite inferior
        graphics.setColor(this.lineColor);
        graphics.drawLine(this.lineInit, this.lineEnd, this.lineThickness);

        // Recorre las bolas que han colisionado y las pintas
        if (!this.collidedBubbles.isEmpty()) {
            for (AnimCollidedBubbles anim : this.collidedBubbles) {
                this.bubbleColors.drawBall(graphics, scene.getGameManager(), anim.color, anim.pos, (int) anim.radius);
            }
        }

        // Recorre las bolas caidas y las pinta
        if (!this.fallingBubbles.isEmpty()) {
            for (Pair<Vector, Integer> bubble : this.fallingBubbles) {
                this.bubbleColors.drawBall(graphics, scene.getGameManager(), bubble.getSecond(), bubble.getFirst(), this.r);
            }
        }
    }

    @Override
    public void update(double deltaTime) {
        // Animacion para cuando colisionan un grupo de bolas
        if (!this.collidedBubbles.isEmpty()) {
            // Para tener la posibilidad de eliminar elementos mientras se recorre,
            // realizamos la actualizacion con iteradores
            Iterator<AnimCollidedBubbles> iterator = this.collidedBubbles.iterator();
            while (iterator.hasNext()) {
                AnimCollidedBubbles anim = iterator.next();
                anim.radius -= this.shrinkSpeed * (float) deltaTime;
                // Si el tamano de bola se se hizo demasiado pequeno, se elimina
                if (anim.radius <= 0f) {
                    iterator.remove();
                }
            }
        }
        // Animacion para bolas que caen
        else if (!this.fallingBubbles.isEmpty()) {
            // Para tener la posibilidad de eliminar elementos mientras se recorre,
            // realizamos la actualizacion con iteradores
            Iterator<Pair<Vector, Integer>> iterator = this.fallingBubbles.iterator();
            while (iterator.hasNext()) {
                Pair<Vector, Integer> bubble = iterator.next();
                bubble.getFirst().y += this.fallingSpeed * (float) deltaTime;

                // Si la posición de la bola ya se salió de la pantalla, eliminarla
                if (bubble.getFirst().y + (float) this.r > this.scene.getWorldHeight()) {
                    iterator.remove();
                }
            }
        }
        else if (this.won) {
            this.audio.stopSound(this.attachSound);
            this.audio.stopSound(this.explosionSound);
            this.end = true;
        }
    }

    @Override
    public void dereference() {
        super.dereference();

        this.bubbleColors = null;
        this.bubbles = null;
        this.colorCount = null;
        this.fallingBubbles.clear();
        this.visited = null;
        this.collidedBubbles.clear();
        this.showGridButton = null;
        this.attachSound = null;
        this.explosionSound = null;
    }

    private boolean tryLoadingProgress(JSONObject progressJson) {
        if (progressJson != null) {
            // Si hay informacion guardada (ya sea del modo normal o modo nivel cargado)
            if (progressJson.has("grid")) {
                int[][] readBubbles = convertJSONArrayToMatrix(progressJson.getJSONArray("grid"));
                int rws = readBubbles.length;

                for (int i = 0; i < rws; i++) {
                    // En las filas impares hay una bola menos
                    int bPerRow = (i % 2 == 0) ? this.cols : (this.cols - 1);

                    // Se generan las burbujas de la fila
                    for (int j = 0; j < bPerRow; j++) {
                        int color = readBubbles[i][j];

                        if (color >= 0 && color < this.bubbleColors.getTotalColors()) {
                            this.bubbles[i][j] = color;
                            this.bubbleColors.addColor(color);
                            this.colorCount[color]++;
                            this.totalBubbles++;
                        }
                        else if (color > this.bubbleColors.getTotalColors()){
                            System.out.println("Invalid bubble color at (" + i + ", " + j + "). Skipping color");
                        }
                    }
                }
            }

            if (progressJson.has("score")) {
                this.score = progressJson.getInt("score");
            }
            return true;
        }
        return false;
    }

    // Convierte posiciones i,j de la matriz a coordenadas de mundo
    private Vector gridToWorldPosition(int i, int j) {
        Vector pos = new Vector(0, 0);
        pos.x = this.offsetX + ((i % 2 == 0) ? 0 : this.r) + this.r + this.r * 2 * j;
        pos.y = this.offsetY + this.r + (this.r * 2 * i) - this.bubbleOffset * i;

        pos.x = Math.round(pos.x);
        pos.y = Math.round(pos.y);
        return pos;
    }

    // Convierte coordenadas de mundo a posiciones i,j de la matriz
    private Pair<Integer, Integer> worldToGridPosition(Vector pos) {
        float y = (pos.y - this.offsetY) / (this.r * 2);
        y += (this.bubbleOffset * y) / (this.r * 2);

        float x = (pos.x - this.offsetX - ((y % 2 == 0) ? 0 : this.r)) / (this.r * 2);

        // Se redondea x por si la bola esta mas hacia un lado o hacia el otro,
        // pero y no se redondea porque se se necesita la posicion de la parte superior
        int i = (int) y;
        int j = (i % 2 == 0) ? Math.round(x) : (int) x;

        return new Pair<>(i, j);
    }

    private boolean cellWithinGrid(int i, int j) {
        return i >= 0 && j >= 0 && i < this.rows && j < ((i % 2 == 0) ? this.cols : this.cols - 1);
    }

    private boolean cellOccupied(Vector pos, int i, int j) {
        if (cellWithinGrid(i, j)) {
            if (this.bubbles[i][j] >= 0) {
                Vector laterals = gridToWorldPosition(i, j);
                return (laterals.distance(pos) < this.r * 2);
            }
        }
        return false;
    }

    private boolean roofCell(int i, int j) {
        return i == 0 && cellWithinGrid(i, j);
    }

    private void updateScore(int bubblesToEraseSize) {
        // Si el grupo es mayor que el limite establecido, aumenta la puntuacion
        if (bubblesToEraseSize >= this.bubblesToExplode + 1) {
            this.score += bubblesToEraseSize * this.greatScore;
        } else {
            this.score += bubblesToEraseSize * this.smallScore;
        }
        updateScoreText();
    }

    private void updateScoreText() {
        Text scoreTextRef = this.scoreText.get();
        if (scoreTextRef != null) {
            scoreTextRef.setTextLine("Score: " + this.score);
        }
    }

    public boolean checkCollision(Vector pos, int color) {
        boolean hasCollided = false;
        Pair<Integer, Integer> rowCol = worldToGridPosition(pos);

        int i = rowCol.getFirst();
        int j = rowCol.getSecond();

        // Se comprueban las casillas adyacentes y si hay alguna ocupada, se marca que ha colisionado
        hasCollided |= cellOccupied(pos, i - 1, (i % 2 == 0) ? j - 1 : j + 1);
        hasCollided |= cellOccupied(pos, i - 1, j);
        hasCollided |= cellOccupied(pos, i, j - 1);
        hasCollided |= cellOccupied(pos, i, j + 1);
        hasCollided &= cellWithinGrid(i, j);

        // DEBUG DE LAS CELDAS
        this.currI = i;
        this.currJ = j;

        // Si no ha colisionado tras comprobar las casillas contiguas, habra
        // colisionado con la pared superior si llega a la primera fila
        if (!hasCollided) {
            hasCollided = i <= 0;
        }

        // Si ha colisionado
        if (hasCollided && cellWithinGrid(i, j)) {
            // DEBUG DE LAS CELDAS
            this.currI = -1;
            this.currJ = -1;

            this.totalBubbles += 1;
            ++this.colorCount[color];
            this.bubbles[i][j] = color;

            if (manageCollision(i, j)) {
                this.won = true;
            }
            // Condicion de derrota
            else if (this.bubbles[i][j] >= 0 && gridToWorldPosition(i, j).y + this.r > this.lineEnd.y) {
                this.audio.stopSound(this.attachSound);
                this.audio.stopSound(this.explosionSound);
                this.end = true;
                this.won = false;
            }
        }
        return hasCollided;
    }

    private boolean manageCollision(int i, int j) {
        this.clearVisited();
        // El valor por defecto de un booleano es false
        int color = this.bubbles[i][j];
        List<Pair<Integer, Integer>> bubblesToErase = new ArrayList<>();
        List<Pair<Integer, Integer>> bubblesToFall = new ArrayList<>();

        dfs(i, j, color, bubblesToErase, bubblesToFall);

        // Numero de bolas del grupo
        int bubblesToEraseSize = bubblesToErase.size();
        // Si se supera el limite establecido, se eliminan
        if (bubblesToEraseSize >= this.bubblesToExplode) {
            this.audio.playSound(this.explosionSound);

            updateScore(bubblesToEraseSize);

            // Se actualiza el numero de bolas totales
            this.totalBubbles -= bubblesToEraseSize;
            // Se actualiza el numero de bolas que hay de cada color
            this.colorCount[color] -= bubblesToEraseSize;
            if (this.colorCount[color] <= 0) {
                this.bubbleColors.removeColor(color);
            }
            // Se actualiza el mapa
            for (Pair<Integer, Integer> bubble : bubblesToErase) {
                int bubbleX = bubble.getFirst();
                int bubbleY = bubble.getSecond();

                // Animacion de las bolas que se juntan
                AnimCollidedBubbles anim = new AnimCollidedBubbles();
                anim.color = this.bubbles[bubbleX][bubbleY];
                anim.pos = gridToWorldPosition(bubbleX, bubbleY);
                anim.radius = this.r;
                this.collidedBubbles.add(anim);

                this.bubbles[bubbleX][bubbleY] = -1;
            }
            // Si no quedan bolas, se ha ganado
            if (this.totalBubbles <= 0) {
                return true;
            }
            // Si siguen quedando bolas, se comprueba si hay bolas que se pueden caer
            return manageFall(bubblesToFall);
        } else {
            this.audio.playSound(this.attachSound);
        }
        return false;
    }

    // A partir de la lista de coordenadas adyacentes, obtenida tras la eliminacion de la bolas del mismo color
    // bolas, se sacan los diferentes conjuntos con dfs y se comprueba si en cada conjunto hay al menos una bola
    // pegada al techo. En ese caso, todas las bolas de ese conjunto no se caen
    private boolean manageFall(List<Pair<Integer, Integer>> bubblesToFall) {
        int numBubblesToFall = 0;
        for (Pair<Integer, Integer> v : bubblesToFall) {
            int vX = v.getFirst();
            int vY = v.getSecond();
            // Se comprueba que sea distinto de nulo porque dos bolas pueden ser del
            // mismo conjunto y se puede haber eliminado ya la coordenada
            if (this.bubbles[vX][vY] >= 0) {
                List<Pair<Integer, Integer>> bubbles = new ArrayList<>();
                this.clearVisited();
                // Si no hay ninguna bola del conjunto que toque el techo, se eliminan
                if (!dfs(vX, vY, bubbles) && !bubbles.isEmpty()) {
                    for (Pair<Integer, Integer> w : bubbles) {
                        // Se guardan en una lista de bolas en movimiento simulando la caida
                        int wX = w.getFirst();
                        int wY = w.getSecond();
                        Vector worldPos = gridToWorldPosition(wX, wY);
                        int color = this.bubbles[wX][wY];
                        if (--this.colorCount[color] <= 0) {
                            this.bubbleColors.removeColor(color);
                        }
                        this.fallingBubbles.add(new Pair<>(worldPos, color));
                        // Se quitan del grid
                        this.bubbles[wX][wY] = -1;
                    }
                    numBubblesToFall += bubbles.size();
                }
            }
        }
        this.totalBubbles -= numBubblesToFall;
        // Devolver indicando si todavia quedan bolas o no
        return this.totalBubbles <= 0;
    }

    private void dfs(int i, int j, int color, List<Pair<Integer, Integer>> bubblesToErase,
                     List<Pair<Integer, Integer>> bubblesToFall) {
        this.visited[i][j] = true;

        int currBubbleCol = this.bubbles[i][j];
        // Si son del mismo color que la bola lanzada, se anade para eliminar
        Pair<Integer, Integer> bubblePos = new Pair<>(i, j);

        if (color == currBubbleCol) {
            bubblesToErase.add(bubblePos);
        }
        // Si no, se trata de una bola adyacente y se tendra en cuenta para la caida...
        else {
            bubblesToFall.add(bubblePos);
        }

        if (color == currBubbleCol) {
            List<Pair<Integer, Integer>> adjacentCells = (i % 2 == 0) ?
                    this.evenAdjacentCells : this.oddAdjacentCells;
            for (Pair<Integer, Integer> dir : adjacentCells) {
                int ni = i + dir.getFirst();
                int nj = j + dir.getSecond();
                // Si es una posicion correcta dentro del mapa...
                if (cellWithinGrid(ni, nj)) {
                    // Si hay bola y no esta visitada...
                    if (this.bubbles[ni][nj] >= 0 && !this.visited[ni][nj]) {
                        dfs(ni, nj, color, bubblesToErase, bubblesToFall);
                    }
                }
            }
        }
    }

    private boolean dfs(int i, int j, List<Pair<Integer, Integer>> bubbles) {
        this.visited[i][j] = true;
        boolean isRoof = roofCell(i, j);
        if (isRoof) return true;
        bubbles.add(new Pair<>(i, j));
        List<Pair<Integer, Integer>> adjacentCells = (i % 2 == 0) ?
                this.evenAdjacentCells : this.oddAdjacentCells;
        for (Pair<Integer, Integer> dir : adjacentCells) {
            int ni = i + dir.getFirst();
            int nj = j + dir.getSecond();
            // Si es una posicion correcta dentro del mapa...
            if (cellWithinGrid(ni, nj)) {
                if (this.bubbles[ni][nj] >= 0 && !this.visited[ni][nj]) {
                    // Si se devuelve true, es que el conjunto calculado tras la recursion esta pegado al techo
                    if (dfs(ni, nj, bubbles))
                        return true;
                }
            }
        }
        return false;
    }

    private void clearVisited() {
        for (int i = 0; i < this.rows; ++i) {
            for (int j = 0; j < this.cols; ++j) {
                this.visited[i][j] = false;
            }
        }
    }


    public boolean hasEnded() {
        return this.end;
    }

    public boolean hasWon() {
        return this.won;
    }

    public int getScore() {
        return this.score;
    }

    public int[][] getBubbles() {
        return this.bubbles;
    }

    private void debugCollisions(IGraphics graphics) {
        if (this.currI >= 0 && this.currJ >= 0) {
            Vector pos = gridToWorldPosition(this.currI, this.currJ);
            pos.x += 0.5f;
            graphics.setColor(this.bubbleColors.getColor(0));
            graphics.drawHexagon(pos, this.hexagonRadius, 90, this.lineThickness * 2);

            pos = gridToWorldPosition(this.currI, this.currJ - 1);
            pos.x += 0.5f;
            graphics.setColor(this.bubbleColors.getColor(1));
            graphics.drawHexagon(pos, this.hexagonRadius, 90, this.lineThickness * 2);

            pos = gridToWorldPosition(this.currI, this.currJ + 1);
            pos.x += 0.5f;
            graphics.setColor(this.bubbleColors.getColor(1));
            graphics.drawHexagon(pos, this.hexagonRadius, 90, this.lineThickness * 2);

            pos = gridToWorldPosition(this.currI - 1, this.currJ);
            pos.x += 0.5f;
            graphics.setColor(this.bubbleColors.getColor(1));
            graphics.drawHexagon(pos, this.hexagonRadius, 90, this.lineThickness * 2);

            pos = gridToWorldPosition(this.currI - 1, (this.currI % 2 == 0) ? this.currJ - 1 : this.currJ + 1);
            pos.x += 0.5f;
            graphics.setColor(this.bubbleColors.getColor(1));
            graphics.drawHexagon(pos, this.hexagonRadius, 90, this.lineThickness * 2);
        }
    }

}
