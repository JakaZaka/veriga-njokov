import { useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { UserContext } from "../userContext";
import { GiClothes } from "react-icons/gi";
import '../TabNav.css';

function Header(props) {
    const userContext = useContext(UserContext);
    const user = userContext.user;
    const title = props.title || "Closy";
    const navigate = useNavigate();

    return (
        <header className="main-header" style={{ background: "#fafdff", boxShadow: "0 2px 8px rgba(25,118,210,0.06)" }}>
            <div className="header-container" style={{ display: "flex", flexDirection: "row", alignItems: "center", width: "100%" }}>
                <div
                    className="logo"
                    onClick={() => navigate('/')}
                    style={{
                        cursor: "pointer",
                        color: "#1976d2",
                        fontWeight: 900,
                        letterSpacing: "3px",
                        fontSize: "3.5rem",
                        margin: "0 32px 0 0",
                        padding: "22px 0 12px 0",
                        userSelect: "none",
                        transition: "color 0.15s",
                        fontFamily: "'Montserrat', 'Segoe UI', Arial, sans-serif",
                        display: "flex",
                        alignItems: "center",
                        gap: "22px",
                        justifyContent: "flex-start"
                    }}
                    title="Go to Home"
                >
                    <GiClothes size={56} style={{ marginBottom: 2 }} />
                    {title}
                </div>
                <nav style={{ width: "100%" }}>
                    <ul className="tab-nav">
                        <li><Link to="/">Home</Link></li>
                        <li><Link to="/weather">Weather</Link></li>
                        {user ? (
                            <>
                                <li><Link to="/explore">Explore</Link></li>
                                <li><Link to="/profile">Profile</Link></li>
                                <li><Link to="/logout">Logout</Link></li>
                                <li><Link to="/map">Map</Link></li>
                                <li><Link to="/outfits">Outfits</Link></li>
                                {userContext.user.role === 'admin' && (
                                    <>
                                        <li><Link to="/admin">Admin</Link></li>
                                        <li><Link to="/stores">Stores</Link></li>
                                        <li><Link to="/addClothingStore">Add Store</Link></li>
                                        <li><Link to="/addClothingStoreLocation">Add Store Location</Link></li>
                                    </>
                                )}
                                <li><Link to="/addClothingItem">Add Item</Link></li>
                                <li><Link to="/addOutfit">Add Outfit</Link></li>
                            </>
                        ) : (
                            <>
                                <li><Link to="/register">Register</Link></li>
                                <li><Link to="/login">Login</Link></li>
                            </>
                        )}
                    </ul>
                </nav>
            </div>
        </header>
    );
}

export default Header;