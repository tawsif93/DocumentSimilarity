import java.util.Comparator;

/**
 * Created by peacefrog on 7/3/16.
 * 7:40 PM
 */
public class Developer implements Comparator<Developer>{
	private String name ;
	private Double summarySimilarity ;
	private Double descriptionSimilarity ;
	private Double summedSimilarity ;


	public Developer() {
	}

	public Developer(String name, Double summarySimilarity, Double descriptionSimilarity) {
		this.name = name;
		this.summarySimilarity = summarySimilarity;
		this.descriptionSimilarity = descriptionSimilarity;

		this.summedSimilarity = summarySimilarity+descriptionSimilarity;
	}

	public String getName() {
		return name;
	}

	public Double getSummarySimilarity() {
		return summarySimilarity;
	}

	public Double getDescriptionSimilarity() {
		return descriptionSimilarity;
	}

	public Double getSummedSimilarity() {
		return summedSimilarity;
	}

	@Override
	public String toString() {
		return "Developer{" +
				"name= '" + name + '\'' +
				", summarySimilarity= " + summarySimilarity +
				", descriptionSimilarity= " + descriptionSimilarity +
				'}';
	}

	@Override
	public int compare(Developer o1, Developer o2) {
		return o1.getName().compareTo(o2.getName());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Developer developer = (Developer) o;

		return getName().equals(developer.getName());

	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}
}
