import { useContext } from "react";
import { Link } from "react-router-dom";
import { UserContext } from "../userContext";
import '../TabNav.css';

function Header(props) {
    const userContext = useContext(UserContext);
    const user = userContext.user;
    const title = props.title || "Closy";

  return (
    <header className="main-header">
      <div className="header-container">
        <h1 className="logo">{title}</h1>
        <nav>
          <ul className="tab-nav">
            <li><Link to="/">Home</Link></li>
            <li><Link to="/explore">Explore</Link></li>
            <li><Link to="/outfits">Outfits</Link></li>
            <li><Link to="/weather">Weather</Link></li>
            <li><Link to="/stores">Stores</Link></li>
            
            {user ? (
              <>
                <li><Link to="/profile">Profile</Link></li>
                <li><Link to="/logout">Logout</Link></li>
                {userContext.user.role === 'admin' && (
                  <>
                      <li><Link to="/admin">Admin</Link></li>
                      <li><Link to="/addClothingStore">Add Store</Link></li>
                      <li><Link to="/addClothingStoreLocation">Add Store Location</Link></li>
                  </>
                )}
                <li><Link to="/addClothingItem">Add Item</Link></li>
                <li><Link to="/addOutfit">Add Outfit</Link></li>
                
                <li><Link to="/stores">Stores</Link></li>
                <li><Link to="/map">Map</Link></li>
                <li><Link to="/outfits">Outfits</Link></li>
               
                
              </>
            ) : (
              <>
                <li><Link to="/register">Register</Link></li>
                <li><Link to="/login">Login</Link></li>
                <li><Link to="/map">Publish</Link></li>
              </>
            )}
          </ul>
        </nav>
      </div>
    </header>
  );
}

export default Header;