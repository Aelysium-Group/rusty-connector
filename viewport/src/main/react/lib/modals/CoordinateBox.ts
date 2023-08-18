import { Coordinate } from "./Coordinate";

export class CoordinateBox {
    private coordinate1: Coordinate;
    private coordinate2: Coordinate;

    constructor(topLeft: Coordinate, bottomRight: Coordinate) {
        this.coordinate1 = topLeft;
        this.coordinate2 = bottomRight;
    }

    /**
     * Get the coordinates of the top left of the box.
     * This is also called the origin.
     * @returns {@link Coordinate}
     */
    public topLeft = () => this.coordinate1;

    /**
     * Get the coordinates of the bottom right of the box.
     * @returns {@link Coordinate}
     */
    public bottomRight = () => this.coordinate1;

    /**
     * Returns a {@link Coordinate} representing the difference between the top left and the bottom right of the box.
     * In other words, returns a {@link Coordinate} representing the width(X) and height(Y) of the box.
     * @returns {@link Coordinate}
     */
    public offestFromOrigin = () => new Coordinate(this.coordinate1.x - this.coordinate2.x, this.coordinate1.y - this.coordinate2.y);

    public static empty = () => new CoordinateBox(new Coordinate(0,0), new Coordinate(0, 0));
}