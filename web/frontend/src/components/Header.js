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
        <header
            className="main-header"
            style={{
                background: "#fffaf6", // light peach
                boxShadow: "0 2px 8px rgba(34,86,34,0.08)"
            }}
        >
            <div
                className="header-container"
                style={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    width: "100%"
                }}
            >
                <div
                    className="logo"
                    onClick={() => navigate('/')}
                    style={{
                        cursor: "pointer",
                        color: "#225622", // dark green
                        fontWeight: 500,
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
                    <GiClothes size={56} style={{ marginBottom: 2, color: "#225622" }} />
                    {title}
                </div>
                <nav style={{ width: "100%" }}>
                    <ul className="tab-nav">
                        {user ? (
                            <>
                                {userContext.user.role === 'admin' && (
                                    <>
                                        <li><Link to="/admin">Admin</Link></li>
                                        <li><Link to="/stores">Stores</Link></li>
                                        <li><Link to="/addClothingStore">Add Store</Link></li>
                                        <li><Link to="/addClothingStoreLocation">Add Store Location</Link></li>
                                    </>
                                )}
                            </>
                        ) : (
                            <>
                            </>
                        )}
                    </ul>
                </nav>
            </div>
        </header>
    );
}

export default Header;