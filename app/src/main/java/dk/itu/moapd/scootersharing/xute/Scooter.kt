package dk.itu.moapd.scootersharing.xute;

public class Scooter {
    private var name: String
    private var location: String

    constructor (name: String, location: String) {
        this.name = name
        this.location = location
    }

    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getLocation(): String {
        return location
    }

    fun setLocation(location: String) {
        this.location = location
    }

    override fun toString(): String {
        return "[ Scooter ] $name is placed at $location ."
    }
}
