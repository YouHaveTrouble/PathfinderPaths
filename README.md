# PathfinderPath

PathfinderPath is an API that allows you to use minecraft pathfinder to get a list of locations from the starting point
up until the goal.

## Usage

This will initialize the path from location1 to location2 with pathfinder limit of 120 steps.
```java
Path path = new Path(location1, location2, 120);
```

This snippet demonstrates how to calculate path between the locations. Only result of SUCCESS will update the path list.
```java
PathCalculationResult result = path.recalculatePath();

if (result.equals(PathCalculationResult.SUCCESS)) {
    List<Location> locations = path.getPath();
}
```