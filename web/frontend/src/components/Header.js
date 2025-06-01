import { useContext } from "react";
import { UserContext } from "../userContext";
import { Link } from "react-router-dom";

function Header(props) {
    const userContext = useContext(UserContext);

    return (
        <header className="main-header">
            <div className="header-container">
                <h1 className="logo">{props.title}</h1>
                <nav>
                    <ul className="nav-links">
                        <li><Link to="/">Home</Link></li>
                        {userContext.user ? (
                            <>
                                <li><Link to="/addClothingItem">Add Item</Link></li>
                                {userContext.user.role === 'admin' && (
                                    <li><Link to="/admin">Admin</Link></li>
                                )}
                                <li><Link to="/profile">Profile</Link></li>
                                <li><Link to="/logout">Logout</Link></li>
                                <li><Link to="/addClothingStore">Add Store</Link></li>
                                <li><Link to="/addClothingStoreLocation">Add store location</Link></li>
                                <li><Link to="/stores">Stores</Link></li>
                            </>
                        ) : (
                            <>
                                <li><Link to="/login">Login</Link></li>
                                <li><Link to="/register">Register</Link></li>
                                <li><Link to="/addClothingItem">Publish</Link></li>
                            </>
                        )}
                    </ul>
                </nav>
            </div>
        </header>
    );
}

export default Header;