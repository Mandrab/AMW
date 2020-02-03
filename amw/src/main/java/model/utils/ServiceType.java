package model.utils;

public enum ServiceType {

	MANAGEMENT_ORDERS( "management( orders )" ),
	ACCEPT_ORDER( "accept( order )" ),
	MANAGEMENT_ITEMS( "management( items )" ),
	INFO_WAREHOUSE( "info( warehouse )" ),
	MANAGEMENT_COMMANDS( "management( commands )"),
	//ADD_COMMANDS( "add( commands )" ),
	INFO_COMMANDS( "info( commands )" );
	//ABILITY_STORE( "ability store" );

	private String name;

	ServiceType(String s ) {
		this.name = s;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
