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
    listening_mode: str = Field(..., examples=["Academia"])
    plays_instrument: bool = Field(False, examples=[True])

    # Dados da recomendação (tabela recommendations)
    score: int = Field(..., ge=10, le=100, examples=[71])
    archetype: str = Field(..., examples=["Rock & Alternativo"])
    recommended_song: str = Field(..., min_length=1, max_length=150, examples=["Paranoid Android"])
    song_artist: str = Field(..., min_length=1, max_length=100, examples=["Radiohead"])
    subgenres: str = Field("", examples=["Eletrônica"])
    liked: bool = Field(..., examples=[True])


class RecommendationUpdate(BaseModel):
    """Corpo do PUT /recommendations/{id}: a pessoa pode mudar de ideia sobre a música."""
    liked: bool = Field(..., examples=[False])


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
    liked: bool
    created_at: datetime
    user: UserOut
