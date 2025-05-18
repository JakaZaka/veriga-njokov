import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { UserContext } from "./userContext";
import Header from "./components/Header";
import ClothingItems from "./components/ClothingIntems";
//import Login from "./components/Login";
//import Register from "./components/Register";
//import Profile from "./components/Profile";
//import Logout from "./components/Logout";
import AddClothingItem from "./components/AddClothingIntem";
import ClothingItemInfo from "./components/ClothingIntemInfo";
import { useState } from 'react';
//import './style.css';

function App() {

  const [user, setUser] = useState(localStorage.user ? JSON.parse(localStorage.user) : null);
  const updateUserData = (userInfo) => {
    localStorage.setItem("user", JSON.stringify(userInfo));
    setUser(userInfo);
  }

  return (
    <BrowserRouter>
      <UserContext.Provider value={{
        user: user,
        setUserContext: updateUserData
      }}>
        <div className="App">
          <Header title="Closy"></Header>
          <Routes>
            <Route path="/" exact element={<ClothingItems />}></Route>
            <Route path="/addClothingItem" element={<AddClothingItem />}></Route>
            <Route path="/clothingItem/:id" element={<ClothingItemInfo />}></Route>
          </Routes>
        </div>
      </UserContext.Provider>
    </BrowserRouter>
  );
}

export default App;
