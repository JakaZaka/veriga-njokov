import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Register from './components/Register';
import Login from './components/Login';
import Logout from './components/Logout';
import Profile from './components/Profile';
import Header from './components/Header';
import ClothingItems from './components/ClothingIntems';
import AddClothingItem from './components/AddClothingIntem';
import ClothingItemInfo from './components/ClothingIntemInfo'; 
import { UserContext } from './userContext';

function App() {
  const [user, setUser] = useState(localStorage.user ? JSON.parse(localStorage.user) : null);

  const updateUserData = (userInfo) => {
    localStorage.setItem("user", JSON.stringify(userInfo));
    setUser(userInfo);
  };

  return (
    <Router>
      <UserContext.Provider value={{ user, setUserContext: updateUserData }}>
        <div className="App">
          <Header title="Closy" />
          <nav>
            <Link to="/">Home</Link> |{' '}
            <Link to="/register">Register</Link> |{' '}
            <Link to="/login">Login</Link> |{' '}
            {user && (
              <>
                <Link to="/logout">Logout</Link> |{' '}
                <Link to="/profile">Profile</Link> |{' '}
                <Link to="/addClothingItem">Add Item</Link>
              </>
            )}
          </nav>
          <Routes>
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<Login />} />
            <Route path="/logout" element={<Logout />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/" element={<ClothingItems />} />
            <Route path="/addClothingItem" element={<AddClothingItem />} />
            <Route path="/clothingItem/:id" element={<ClothingItemInfo />} />
          </Routes>
        </div>
      </UserContext.Provider>
    </Router>
  );
}

export default App;