"""
Schemas Pydantic: definem o formato do JSON que entra (request) e sai (response)
da API. Também geram automaticamente a documentação e os exemplos do Swagger.
"""
from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


# ---------- Usuário ----------
class UserOut(BaseModel):
    """Dados do usuário devolvidos aninhados dentro da recomendação."""
    model_config = ConfigDict(from_attributes=True)  # permite converter objeto ORM -> JSON

    id: int
    name: str
    favorite_artist: str
    listening_mode: str
    plays_instrument: bool
    created_at: datetime


# ---------- Recomendação ----------
class RecommendationCreate(BaseModel):
    """Corpo do POST /recommendations: dados do perfil + resultado do quiz."""
    # Dados do usuário (tabela users)
    name: str = Field(..., min_length=1, max_length=100, examples=["Pedro"])
    favorite_artist: str = Field(..., min_length=1, max_length=100, examples=["Arctic Monkeys"])
    listening_mode: str = Field(..., examples=["Fone de Ouvido"])
    plays_instrument: bool = Field(False, examples=[True])

    # Dados da recomendação (tabela recommendations)
    score: int = Field(..., ge=10, le=100, examples=[72])
    archetype: str = Field(..., examples=["Groove & Funk Rock Alternativo"])
    recommended_song: str = Field(..., min_length=1, max_length=150, examples=["Give It Away"])
    song_artist: str = Field(..., min_length=1, max_length=100, examples=["Red Hot Chili Peppers"])
    subgenres: str = Field("", examples=["Hard Rock, Funk/Alternative"])
    personal_note: str = Field("", max_length=500, examples=["Curti muito o baixo do Flea!"])


class RecommendationUpdate(BaseModel):
    """Corpo do PUT /recommendations/{id}: apenas a nota pessoal é editável."""
    personal_note: str = Field(..., max_length=500, examples=["Ouvi de novo, virou meu favorito."])


class RecommendationOut(BaseModel):
    """Resposta da API: recomendação com o usuário aninhado."""
    model_config = ConfigDict(from_attributes=True)

    id: int
    user_id: int
    score: int
    archetype: str
    recommended_song: str
    song_artist: str
    subgenres: str
    personal_note: str
    created_at: datetime
    user: UserOut
